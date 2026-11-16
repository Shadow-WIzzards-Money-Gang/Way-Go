package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.RotaResponse;
import com.bytestorm.waygo.application.dto.response.TrechoRotaResponse;
import com.bytestorm.waygo.application.event.StatusLinhaAlteradoEvent;
import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.entity.Notificacao;
import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.enums.Plataforma;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;
import com.bytestorm.waygo.domain.enums.TipoTransporte;

/** 16/11/2026 é uma segunda-feira. O cálculo de rotas é simulado; o status vem do cache real. */
class NotificacaoRotaServiceTest {

	private static final LocalDateTime SEGUNDA_7H = LocalDateTime.of(2026, 11, 16, 7, 0);
	private static final UUID ANA = UUID.randomUUID();
	private static final UUID BRUNO = UUID.randomUUID();
	private static final UUID CARLA = UUID.randomUUID();

	private final NotificacoesEmMemoria.Relogio relogio = new NotificacoesEmMemoria.Relogio(SEGUNDA_7H);
	private final RedeEmMemoria rede = new RedeEmMemoria();
	private final NotificacoesEmMemoria.Notificacoes notificacoes = new NotificacoesEmMemoria.Notificacoes();
	private final NotificacoesEmMemoria.Dispositivos dispositivos = new NotificacoesEmMemoria.Dispositivos();
	private final NotificacoesEmMemoria.RotasSalvas rotas = new NotificacoesEmMemoria.RotasSalvas();
	private final NotificacoesEmMemoria.Agendamentos agendamentos = new NotificacoesEmMemoria.Agendamentos();
	private final RotaService rotaService = mock(RotaService.class);
	private final NotificacaoRotaService service = new NotificacaoRotaService(agendamentos, rotas, notificacoes,
			dispositivos, rede.linhas, rede.estacoes, rede.statusCache(relogio), rotaService, relogio);

	private final LinhaTransporte azul = linha("Linha 1-Azul", "1");
	private final LinhaTransporte vermelha = linha("Linha 3-Vermelha", "3");
	private final LinhaTransporte amarela = linha("Linha 4-Amarela", "4");
	private final Long se = rede.estacoes.salvar(Estacao.criar("Sé", null)).getId();
	private final Long luz = rede.estacoes.salvar(Estacao.criar("Luz", null)).getId();

	/** Rota habitual de cada rota salva (por id) e as rotas "agora" com e sem alternativa. */
	private final Map<Long, RotaResponse> habituais = new HashMap<>();
	private final Map<Long, RotaResponse> alternativas = new HashMap<>();
	private final Map<Long, RotaResponse> atuais = new HashMap<>();

	@BeforeEach
	void simularCalculoDeRotas() {
		when(rotaService.rotaHabitual(any())).thenAnswer(i -> Optional.ofNullable(habituais.get(idDa(i))));
		when(rotaService.rotaAgora(any(), anyBoolean())).thenAnswer(i -> Optional.ofNullable(
				(boolean) i.getArgument(1) ? alternativas.get(idDa(i)) : atuais.get(idDa(i))));
	}

	// --- lembretes (US11) ---

	@Test
	void lembreteSaiNoMomentoDoAvisoUmaVezSoEExpiraNaPartida() {
		RotaSalva casaTrabalho = rota(ANA, "Casa → Trabalho", azul);
		agendar(casaTrabalho, "07:10", 10, DiaSemana.SEGUNDA);

		relogio.definir(SEGUNDA_7H.minusMinutes(1));
		assertThat(service.enfileirarLembretes()).isZero();

		relogio.definir(SEGUNDA_7H);
		assertThat(service.enfileirarLembretes()).isEqualTo(1);
		relogio.definir(SEGUNDA_7H.plusMinutes(5));
		assertThat(service.enfileirarLembretes()).isZero();

		Notificacao lembrete = notificacoes.unica();
		assertThat(lembrete.getTipo()).isEqualTo(TipoNotificacao.LEMBRETE_AGENDAMENTO);
		assertThat(lembrete.getChave()).isEqualTo("agendamento:1:2026-11-16");
		assertThat(lembrete.getExpiraEm()).isEqualTo(LocalDateTime.of(2026, 11, 16, 7, 10));
	}

	@Test
	void backendForaDoArAindaAvisaSeAPartidaNaoPassou() {
		agendar(rota(ANA, "Casa → Trabalho", azul), "07:10", 10, DiaSemana.SEGUNDA);

		relogio.definir(SEGUNDA_7H.plusMinutes(9));
		assertThat(service.enfileirarLembretes()).isEqualTo(1);
	}

	@Test
	void depoisDaPartidaNaoAvisaMais() {
		agendar(rota(ANA, "Casa → Trabalho", azul), "07:10", 10, DiaSemana.SEGUNDA);

		relogio.definir(SEGUNDA_7H.plusMinutes(10));
		assertThat(service.enfileirarLembretes()).isZero();
	}

	@Test
	void antecedenciaQueCruzaAMeiaNoiteAvisaNaVespera() {
		agendar(rota(ANA, "Madrugada", azul), "00:10", 30, DiaSemana.TERCA);

		relogio.definir(LocalDateTime.of(2026, 11, 16, 23, 45));
		assertThat(service.enfileirarLembretes()).isEqualTo(1);
		assertThat(notificacoes.unica().getChave()).isEqualTo("agendamento:1:2026-11-17");
	}

	@Test
	void diaSemAgendamentoNaoAvisa() {
		agendar(rota(ANA, "Fim de semana", azul), "07:10", 10, DiaSemana.SABADO);

		assertThat(service.enfileirarLembretes()).isZero();
	}

	@Test
	void lembreteComRotaOkTrazOTempoDeTrajeto() {
		RotaSalva casaTrabalho = rota(ANA, "Casa → Trabalho", azul);
		agendar(casaTrabalho, "07:10", 10, DiaSemana.SEGUNDA);

		service.enfileirarLembretes();

		Notificacao lembrete = notificacoes.unica();
		assertThat(lembrete.getTitulo()).isEqualTo("Casa → Trabalho: tudo certo");
		assertThat(lembrete.getCorpo()).isEqualTo(
				"Saída às 07:10. Trajeto de ~25 min por Linha 1-Azul, sem problemas nas linhas.");
		assertThat(lembrete.getDados()).containsEntry("alternativa", "false")
				.containsEntry("origemNome", "Sé").containsEntry("destinoNome", "Luz")
				.containsEntry("idRotaSalva", String.valueOf(casaTrabalho.getId()));
	}

	@Test
	void lembreteComLinhaComProblemaOfereceARotaAlternativa() {
		status(vermelha, StatusOperacional.PARALISADA);
		RotaSalva casaTrabalho = rota(ANA, "Casa → Trabalho", vermelha);
		alternativas.put(casaTrabalho.getId(), resposta(41, amarela, azul));
		agendar(casaTrabalho, "07:10", 10, DiaSemana.SEGUNDA);

		service.enfileirarLembretes();

		Notificacao lembrete = notificacoes.unica();
		assertThat(lembrete.getTitulo()).isEqualTo("Atenção na rota Casa → Trabalho");
		assertThat(lembrete.getCorpo()).isEqualTo("Linha 3-Vermelha: Paralisada. Rota alternativa: ~41 min por "
				+ "Linha 4-Amarela e Linha 1-Azul. Saída às 07:10.");
		assertThat(lembrete.getDados()).containsEntry("alternativa", "true");
	}

	@Test
	void lembreteSemAlternativaAvisaQueNaoHaRota() {
		status(vermelha, StatusOperacional.PARALISADA);
		RotaSalva casaTrabalho = rota(ANA, "Casa → Trabalho", vermelha);
		agendar(casaTrabalho, "07:10", 10, DiaSemana.SEGUNDA);

		service.enfileirarLembretes();

		assertThat(notificacoes.unica().getCorpo()).isEqualTo(
				"Linha 3-Vermelha: Paralisada. No momento não há rota disponível. Saída às 07:10.");
	}

	@Test
	void lembreteComLentidaoSemAlternativaDizOTempoEstimado() {
		status(vermelha, StatusOperacional.VELOCIDADE_REDUZIDA);
		RotaSalva casaTrabalho = rota(ANA, "Casa → Trabalho", vermelha);
		atuais.put(casaTrabalho.getId(), resposta(38, vermelha));
		agendar(casaTrabalho, "07:10", 10, DiaSemana.SEGUNDA);

		service.enfileirarLembretes();

		assertThat(notificacoes.unica().getCorpo()).isEqualTo("Linha 3-Vermelha: Velocidade Reduzida. Não há rota "
				+ "só com linhas normais; o trajeto deve levar ~38 min. Saída às 07:10.");
	}

	// --- alertas de status (US15 e US16) ---

	@Test
	void mudancaDeStatusAvisaSoQuemTemRotaPelaLinhaEAlertasLigados() {
		RotaSalva daAna = rota(ANA, "Casa → Trabalho", vermelha);
		rota(BRUNO, "Academia", azul);
		rota(CARLA, "Faculdade", vermelha);
		aparelho(ANA, true);
		aparelho(BRUNO, true);
		aparelho(CARLA, false);
		alternativas.put(daAna.getId(), resposta(41, amarela));

		int criados = service.notificarMudancaStatus(evento(StatusOperacional.NORMAL, StatusOperacional.PARALISADA,
				"Falha na sinalização"));

		assertThat(criados).isEqualTo(1);
		Notificacao alerta = notificacoes.unica();
		assertThat(alerta.getIdUsuario()).isEqualTo(ANA);
		assertThat(alerta.getTipo()).isEqualTo(TipoNotificacao.STATUS_LINHA);
		assertThat(alerta.getTitulo()).isEqualTo("Linha 3-Vermelha: Paralisada");
		assertThat(alerta.getCorpo()).isEqualTo("Afeta Casa → Trabalho. Falha na sinalização. Rota alternativa para "
				+ "Casa → Trabalho: ~41 min por Linha 4-Amarela.");
		assertThat(alerta.getDados()).containsEntry("alternativa", "true");
		assertThat(alerta.getExpiraEm()).isEqualTo(SEGUNDA_7H.plusHours(1));
	}

	@Test
	void umAlertaPorUsuarioMesmoComVariasRotasEMesmoEventoNaoDuplica() {
		rota(ANA, "Casa → Trabalho", vermelha);
		rota(ANA, "Trabalho → Casa", vermelha);
		aparelho(ANA, true);
		StatusLinhaAlteradoEvent evento = evento(StatusOperacional.NORMAL, StatusOperacional.PARALISADA, null);

		service.notificarMudancaStatus(evento);
		service.notificarMudancaStatus(evento);

		assertThat(notificacoes.unica().getCorpo()).startsWith("Afeta Casa → Trabalho e Trabalho → Casa.")
				.endsWith("Não há rota alternativa só com linhas normais no momento.");
	}

	@Test
	void voltaAoNormalAvisaANormalizacao() {
		rota(ANA, "Casa → Trabalho", vermelha);
		aparelho(ANA, true);

		service.notificarMudancaStatus(evento(StatusOperacional.PARALISADA, StatusOperacional.NORMAL, null));

		Notificacao alerta = notificacoes.unica();
		assertThat(alerta.getTitulo()).isEqualTo("Linha 3-Vermelha: operação normal");
		assertThat(alerta.getCorpo())
				.isEqualTo("Linha 3-Vermelha voltou a operar normalmente. Afeta Casa → Trabalho.");
		assertThat(alerta.getDados()).containsEntry("alternativa", "false");
	}

	@Test
	void fechamentoNoturnoEStatusDesconhecidoNaoGeramAlerta() {
		rota(ANA, "Casa → Trabalho", vermelha);
		aparelho(ANA, true);

		assertThat(service.notificarMudancaStatus(
				evento(StatusOperacional.NORMAL, StatusOperacional.OPERACAO_ENCERRADA, null))).isZero();
		assertThat(service.notificarMudancaStatus(
				evento(StatusOperacional.OPERACAO_ENCERRADA, StatusOperacional.NORMAL, null))).isZero();
		assertThat(service.notificarMudancaStatus(
				evento(StatusOperacional.DESCONHECIDO, StatusOperacional.PARALISADA, null))).isZero();
		assertThat(notificacoes.todas()).isEmpty();
	}

	@Test
	void juntarListaNoFormatoDeTexto() {
		assertThat(NotificacaoRotaService.juntar(List.of())).isEmpty();
		assertThat(NotificacaoRotaService.juntar(List.of("A"))).isEqualTo("A");
		assertThat(NotificacaoRotaService.juntar(List.of("A", "B", "C"))).isEqualTo("A, B e C");
	}

	// --- apoio ---

	private LinhaTransporte linha(String nome, String codigo) {
		return rede.linhas.salvar(LinhaTransporte.criar(nome, codigo, "#000000", "Metrô", TipoTransporte.METRO));
	}

	private void status(LinhaTransporte linha, StatusOperacional status) {
		rede.status.salvar(LinhaStatus.registrarPelaApi(linha.getId(), status, null, SEGUNDA_7H.minusHours(1)));
	}

	/**
	 * Rota salva cuja rota habitual passa pelas linhas informadas (25 min). A primeira vai da Sé à Luz;
	 * as outras, para destinos próprios, já que o serviço reaproveita o cálculo de trajetos iguais.
	 */
	private RotaSalva rota(UUID dono, String nome, LinhaTransporte... linhas) {
		Long destino = rotas.listarTodas().isEmpty() ? luz : rede.estacoes.salvar(Estacao.criar(nome, null)).getId();
		RotaSalva salva = rotas.salvar(RotaSalva.criar(se, destino, nome, CriterioRota.EQUILIBRADO, dono, SEGUNDA_7H));
		habituais.put(salva.getId(), resposta(25, linhas));
		return salva;
	}

	private void agendar(RotaSalva rota, String horario, int antecedencia, DiaSemana dia) {
		int minutos = Integer.parseInt(horario.substring(0, 2)) * 60 + Integer.parseInt(horario.substring(3));
		agendamentos.salvar(RotaAgendada.criar(rota.getId(), minutos, antecedencia, Set.of(dia), SEGUNDA_7H));
	}

	private void aparelho(UUID dono, boolean alertasStatus) {
		dispositivos.salvar(DispositivoUsuario.criar(dono, "token-" + dono, Plataforma.ANDROID, alertasStatus,
				SEGUNDA_7H));
	}

	private StatusLinhaAlteradoEvent evento(StatusOperacional anterior, StatusOperacional novo, String descricao) {
		return new StatusLinhaAlteradoEvent(vermelha.getId(), 99L, anterior, novo, descricao);
	}

	private static RotaResponse resposta(int minutos, LinhaTransporte... linhas) {
		List<TrechoRotaResponse> trechos = new java.util.ArrayList<>();
		for (int i = 0; i < linhas.length; i++) {
			trechos.add(new TrechoRotaResponse(i + 1, LinhaResumoResponse.de(linhas[i]), StatusOperacional.NORMAL,
					null, null, null, List.of(), 3, minutos * 60 / linhas.length, 1000));
		}
		return new RotaResponse(null, null, CriterioRota.EQUILIBRADO, false, minutos * 60, minutos, 1000,
				linhas.length - 1, List.of(), trechos, List.of());
	}

	private static Long idDa(org.mockito.invocation.InvocationOnMock invocacao) {
		return ((RotaSalva) invocacao.getArgument(0)).getId();
	}

}
