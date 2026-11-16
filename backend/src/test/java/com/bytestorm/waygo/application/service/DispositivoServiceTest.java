package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.request.DispositivoRequest;
import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.enums.Plataforma;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;

class DispositivoServiceTest {

	private static final UUID ANA = UUID.randomUUID();
	private static final UUID BRUNO = UUID.randomUUID();

	private final NotificacoesEmMemoria.Relogio relogio = new NotificacoesEmMemoria.Relogio(
			LocalDateTime.of(2026, 11, 16, 10, 0));
	private final NotificacoesEmMemoria.Dispositivos dispositivos = new NotificacoesEmMemoria.Dispositivos();
	private final DispositivoService service = new DispositivoService(dispositivos, relogio);

	@Test
	void registraAparelhoNovoAtivoComAlertasLigadosPorPadrao() {
		service.registrar(ANA, new DispositivoRequest(" token-1 ", Plataforma.ANDROID, null));

		DispositivoUsuario aparelho = dispositivos.todos().getFirst();
		assertThat(aparelho.getTokenFcm()).isEqualTo("token-1");
		assertThat(aparelho.getIdUsuario()).isEqualTo(ANA);
		assertThat(aparelho.isAtivo()).isTrue();
		assertThat(aparelho.isAlertasStatus()).isTrue();
	}

	@Test
	void registrarDeNovoAtualizaOsAlertasSemDuplicar() {
		service.registrar(ANA, new DispositivoRequest("token-1", Plataforma.ANDROID, true));
		relogio.avancar(Duration.ofMinutes(5));

		service.registrar(ANA, new DispositivoRequest("token-1", Plataforma.ANDROID, false));

		assertThat(dispositivos.todos()).singleElement().satisfies(d -> {
			assertThat(d.isAlertasStatus()).isFalse();
			assertThat(d.getAtualizadoEm()).isEqualTo(relogio.agora());
		});
	}

	@Test
	void tokenDeOutraContaPassaParaQuemEntrouEVoltaAFicarAtivo() {
		service.registrar(ANA, new DispositivoRequest("token-1", Plataforma.ANDROID, true));
		DispositivoUsuario daAna = dispositivos.buscarPorToken("token-1").orElseThrow();
		daAna.desativar(relogio.agora());
		dispositivos.salvar(daAna);

		service.registrar(BRUNO, new DispositivoRequest("token-1", Plataforma.ANDROID, true));

		assertThat(dispositivos.todos()).singleElement().satisfies(d -> {
			assertThat(d.getIdUsuario()).isEqualTo(BRUNO);
			assertThat(d.isAtivo()).isTrue();
		});
	}

	@Test
	void removerSoApagaOAparelhoDoProprioUsuario() {
		service.registrar(ANA, new DispositivoRequest("token-1", Plataforma.ANDROID, true));

		service.remover(BRUNO, "token-1");
		assertThat(dispositivos.todos()).hasSize(1);

		service.remover(ANA, "token-1");
		assertThat(dispositivos.todos()).isEmpty();
	}

	@Test
	void removerSemTokenEhRequisicaoInvalida() {
		assertThatThrownBy(() -> service.remover(ANA, " ")).isInstanceOf(RequisicaoInvalidaException.class);
	}

}
