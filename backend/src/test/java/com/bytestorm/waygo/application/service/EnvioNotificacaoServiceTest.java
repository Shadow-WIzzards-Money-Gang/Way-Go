package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.entity.Notificacao;
import com.bytestorm.waygo.domain.enums.Plataforma;
import com.bytestorm.waygo.domain.enums.StatusNotificacao;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;
import com.bytestorm.waygo.domain.interfaces.EnviadorPush;
import com.bytestorm.waygo.domain.valueobject.ResultadoEnvioPush;

class EnvioNotificacaoServiceTest {

	private static final UUID ANA = UUID.randomUUID();

	private final NotificacoesEmMemoria.Relogio relogio = new NotificacoesEmMemoria.Relogio(
			LocalDateTime.of(2026, 11, 16, 7, 0));
	private final NotificacoesEmMemoria.Notificacoes notificacoes = new NotificacoesEmMemoria.Notificacoes();
	private final NotificacoesEmMemoria.Dispositivos dispositivos = new NotificacoesEmMemoria.Dispositivos();
	private final PushFalso push = new PushFalso();
	private final EnvioNotificacaoService service = new EnvioNotificacaoService(notificacoes, dispositivos, push,
			relogio);

	/** Responde conforme o token configurado; por padrão, sucesso. */
	private static class PushFalso implements EnviadorPush {

		final Map<String, ResultadoEnvioPush> respostas = new HashMap<>();
		final List<String> enviados = new ArrayList<>();
		final List<Map<String, String>> dados = new ArrayList<>();

		@Override
		public ResultadoEnvioPush enviar(String tokenFcm, String titulo, String corpo, Map<String, String> dados) {
			enviados.add(tokenFcm);
			this.dados.add(dados);
			return respostas.getOrDefault(tokenFcm, ResultadoEnvioPush.sucesso());
		}
	}

	private void aparelho(String token, boolean alertasStatus) {
		dispositivos.salvar(DispositivoUsuario.criar(ANA, token, Plataforma.ANDROID, alertasStatus, relogio.agora()));
	}

	private Notificacao enfileirar(TipoNotificacao tipo, Duration validade) {
		return notificacoes.salvar(Notificacao.criar(ANA, tipo, tipo + ":1", "Título", "Corpo",
				Map.of("origemId", "1"), relogio.agora().plus(validade), relogio.agora()));
	}

	@Test
	void enviaParaTodosOsAparelhosAtivosEMarcaComoEnviada() {
		aparelho("celular", true);
		aparelho("tablet", true);
		Notificacao notificacao = enfileirar(TipoNotificacao.LEMBRETE_AGENDAMENTO, Duration.ofMinutes(10));

		assertThat(service.processarFila()).isEqualTo(1);

		assertThat(push.enviados).containsExactlyInAnyOrder("celular", "tablet");
		assertThat(push.dados.getFirst()).containsEntry("origemId", "1")
				.containsEntry("idNotificacao", String.valueOf(notificacao.getId()))
				.containsEntry("tipo", "LEMBRETE_AGENDAMENTO");
		assertThat(notificacoes.unica().getStatus()).isEqualTo(StatusNotificacao.ENVIADA);

		// Já enviada: não sai de novo.
		relogio.avancar(Duration.ofMinutes(1));
		service.processarFila();
		assertThat(push.enviados).hasSize(2);
	}

	@Test
	void falhaTemporariaReagendaETentaDeNovoAteDarCerto() {
		aparelho("celular", true);
		push.respostas.put("celular", ResultadoEnvioPush.falhaTemporaria("UNAVAILABLE"));
		enfileirar(TipoNotificacao.LEMBRETE_AGENDAMENTO, Duration.ofMinutes(10));

		assertThat(service.processarFila()).isZero();
		Notificacao pendente = notificacoes.unica();
		assertThat(pendente.getStatus()).isEqualTo(StatusNotificacao.PENDENTE);
		assertThat(pendente.getTentativas()).isEqualTo(1);
		assertThat(pendente.getUltimoErro()).isEqualTo("UNAVAILABLE");
		assertThat(pendente.getProximaTentativaEm()).isEqualTo(relogio.agora().plusSeconds(30));

		// Antes da hora da próxima tentativa, não reenvia.
		relogio.avancar(Duration.ofSeconds(10));
		service.processarFila();
		assertThat(push.enviados).hasSize(1);

		push.respostas.clear();
		relogio.avancar(Duration.ofSeconds(20));
		assertThat(service.processarFila()).isEqualTo(1);
		assertThat(notificacoes.unica().getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
		assertThat(notificacoes.unica().getTentativas()).isEqualTo(2);
	}

	@Test
	void tokenInvalidoDesativaOAparelhoEOOutroRecebe() {
		aparelho("antigo", true);
		aparelho("novo", true);
		push.respostas.put("antigo", ResultadoEnvioPush.tokenInvalido("UNREGISTERED"));
		enfileirar(TipoNotificacao.LEMBRETE_AGENDAMENTO, Duration.ofMinutes(10));

		assertThat(service.processarFila()).isEqualTo(1);

		assertThat(dispositivos.buscarPorToken("antigo").orElseThrow().isAtivo()).isFalse();
		assertThat(dispositivos.buscarPorToken("novo").orElseThrow().isAtivo()).isTrue();
		assertThat(notificacoes.unica().getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
	}

	@Test
	void semAparelhoAtivoContaComoFalhaEExpiraNoPrazo() {
		enfileirar(TipoNotificacao.LEMBRETE_AGENDAMENTO, Duration.ofMinutes(1));

		service.processarFila();
		assertThat(notificacoes.unica().getUltimoErro()).isEqualTo(EnvioNotificacaoService.SEM_APARELHO);

		relogio.avancar(Duration.ofMinutes(1));
		service.processarFila();
		assertThat(notificacoes.unica().getStatus()).isEqualTo(StatusNotificacao.EXPIRADA);
		assertThat(push.enviados).isEmpty();
	}

	@Test
	void alertaDeStatusSoVaiParaAparelhosComAlertasLigados() {
		aparelho("com-alertas", true);
		aparelho("sem-alertas", false);
		enfileirar(TipoNotificacao.STATUS_LINHA, Duration.ofHours(1));

		service.processarFila();

		assertThat(push.enviados).containsExactly("com-alertas");
	}

	@Test
	void lembreteVaiMesmoParaAparelhoComAlertasDeStatusDesligados() {
		aparelho("sem-alertas", false);
		enfileirar(TipoNotificacao.LEMBRETE_AGENDAMENTO, Duration.ofMinutes(10));

		service.processarFila();

		assertThat(push.enviados).containsExactly("sem-alertas");
	}

}
