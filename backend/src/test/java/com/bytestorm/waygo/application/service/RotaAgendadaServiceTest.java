package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.request.RotaAgendadaRequest;
import com.bytestorm.waygo.application.dto.response.RotaAgendadaResponse;
import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.RotaAgendadaRepository;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;

/** Só o banco é simulado; as regras do service e do domínio são as reais. */
class RotaAgendadaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-13T12:00:00Z");
	private static final UUID ANA = UUID.randomUUID();
	private static final UUID BRUNO = UUID.randomUUID();

	private final RotasEmMemoria rotas = new RotasEmMemoria();
	private final AgendamentosEmMemoria agendamentos = new AgendamentosEmMemoria(rotas);
	private final RotaAgendadaService service = new RotaAgendadaService(agendamentos, rotas,
			Clock.fixed(AGORA, ZoneOffset.UTC));

	private final Long rotaDaAna = rotas.criar(ANA);
	private final Long outraRotaDaAna = rotas.criar(ANA);
	private final Long rotaDoBruno = rotas.criar(BRUNO);

	private static class RotasEmMemoria implements RotaSalvaRepository {

		private final Map<Long, RotaSalva> rotas = new LinkedHashMap<>();
		private long sequencia;

		Long criar(UUID dono) {
			return salvar(RotaSalva.criar(1L, 2L, "Rota " + (sequencia + 1), CriterioRota.EQUILIBRADO, dono,
					LocalDateTime.ofInstant(AGORA, ZoneOffset.UTC))).getId();
		}

		@Override
		public RotaSalva salvar(RotaSalva rota) {
			Long id = rota.getId() != null ? rota.getId() : ++sequencia;
			RotaSalva salva = RotaSalva.reconstruir(id, rota.getIdEstacaoOrigem(), rota.getIdEstacaoDestino(),
					rota.getNome(), rota.getCriterioRota(), rota.getIdUsuario(), rota.getCriadoEm());
			rotas.put(id, salva);
			return salva;
		}

		@Override
		public Optional<RotaSalva> buscarPorId(Long id) {
			return Optional.ofNullable(rotas.get(id));
		}

		@Override
		public Optional<RotaSalva> buscarPorIdEUsuario(Long id, UUID idUsuario) {
			return buscarPorId(id).filter(rota -> rota.getIdUsuario().equals(idUsuario));
		}

		@Override
		public List<RotaSalva> listarPorUsuario(UUID idUsuario) {
			return rotas.values().stream().filter(r -> r.getIdUsuario().equals(idUsuario)).toList();
		}

		@Override
		public void excluir(Long id) {
			rotas.remove(id);
		}
	}

	private static class AgendamentosEmMemoria implements RotaAgendadaRepository {

		private final Map<Long, RotaAgendada> dados = new LinkedHashMap<>();
		private final RotasEmMemoria rotas;
		private long sequencia;

		AgendamentosEmMemoria(RotasEmMemoria rotas) {
			this.rotas = rotas;
		}

		@Override
		public RotaAgendada salvar(RotaAgendada a) {
			Long id = a.getId() != null ? a.getId() : ++sequencia;
			RotaAgendada salvo = RotaAgendada.reconstruir(id, a.getIdRotaSalva(), a.getHorarioPartidaMinutos(),
					a.getAntecedenciaMinutos(), a.isAtiva(), a.getDiasSemana(), a.getCriadoEm());
			dados.put(id, salvo);
			return salvo;
		}

		@Override
		public Optional<RotaAgendada> buscarPorId(Long id) {
			return Optional.ofNullable(dados.get(id));
		}

		@Override
		public List<RotaAgendada> listarPorRotaSalva(Long idRotaSalva) {
			return dados.values().stream()
					.filter(a -> a.getIdRotaSalva().equals(idRotaSalva))
					.sorted(Comparator.comparingInt(RotaAgendada::getHorarioPartidaMinutos))
					.toList();
		}

		@Override
		public List<RotaAgendada> listarPorUsuario(UUID idUsuario) {
			return dados.values().stream()
					.filter(a -> rotas.buscarPorIdEUsuario(a.getIdRotaSalva(), idUsuario).isPresent())
					.sorted(Comparator.comparingInt(RotaAgendada::getHorarioPartidaMinutos))
					.toList();
		}

		@Override
		public List<RotaAgendada> listarAtivasPorDia(DiaSemana diaSemana) {
			return dados.values().stream().filter(a -> a.isAtiva() && a.executaNo(diaSemana)).toList();
		}

		@Override
		public void excluir(Long id) {
			dados.remove(id);
		}
	}

	private static RotaAgendadaRequest pedido(String horario, Integer antecedencia, List<String> dias, Boolean ativa) {
		return new RotaAgendadaRequest(horario, antecedencia, dias, ativa);
	}

	private RotaAgendadaResponse agendarDaAna(String horario, String... dias) {
		return service.criar(ANA, rotaDaAna, pedido(horario, null, List.of(dias), null));
	}

	// --- criar ---

	@Test
	void criarUsaValoresPadraoEConverteHorarioEDias() {
		RotaAgendadaResponse criado = agendarDaAna("07:10", "SEGUNDA", "Quarta-feira", "sexta");

		assertThat(criado.id()).isNotNull();
		assertThat(criado.idRotaSalva()).isEqualTo(rotaDaAna);
		assertThat(criado.horario()).isEqualTo("07:10");
		assertThat(criado.horarioPartidaMinutos()).isEqualTo(430);
		assertThat(criado.antecedenciaMinutos()).isEqualTo(RotaAgendadaService.ANTECEDENCIA_PADRAO);
		assertThat(criado.ativa()).isTrue();
		assertThat(criado.diasSemana()).containsExactly(DiaSemana.SEGUNDA, DiaSemana.QUARTA, DiaSemana.SEXTA);
		assertThat(criado.criadoEm()).isEqualTo(LocalDateTime.ofInstant(AGORA, ZoneOffset.UTC));
	}

	@Test
	void umaRotaPodeTerVariosAgendamentos() {
		agendarDaAna("18:00", "SEGUNDA");
		agendarDaAna("07:00", "SEGUNDA");
		agendarDaAna("09:30", "SABADO");

		assertThat(service.listarDaRota(ANA, rotaDaAna))
				.extracting(RotaAgendadaResponse::horario)
				.containsExactly("07:00", "09:30", "18:00");
	}

	@Test
	void naoAgendaEmRotaDeOutroUsuario() {
		assertThatThrownBy(() -> service.criar(BRUNO, rotaDaAna, pedido("07:00", null, List.of("SEGUNDA"), null)))
				.isInstanceOf(RecursoNaoEncontradoException.class);
		assertThatThrownBy(() -> service.listarDaRota(BRUNO, rotaDaAna))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void rejeitaDadosInvalidos() {
		assertThatThrownBy(() -> agendarDaAna("25:00", "SEGUNDA")).isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> agendarDaAna("7h", "SEGUNDA")).isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> agendarDaAna("07:00", "FERIADO")).isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.criar(ANA, rotaDaAna, pedido("07:00", null, List.of(), null)))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.criar(ANA, rotaDaAna, pedido("07:00", 2000, List.of("SEGUNDA"), null)))
				.isInstanceOf(RequisicaoInvalidaException.class);
	}

	// --- listar ---

	@Test
	void listarTrazSoOsAgendamentosDoUsuario() {
		agendarDaAna("07:00", "SEGUNDA");
		service.criar(ANA, outraRotaDaAna, pedido("08:00", null, List.of("TERCA"), null));
		service.criar(BRUNO, rotaDoBruno, pedido("06:00", null, List.of("QUINTA"), null));

		assertThat(service.listar(ANA)).extracting(RotaAgendadaResponse::horario).containsExactly("07:00", "08:00");
		assertThat(service.listar(BRUNO)).extracting(RotaAgendadaResponse::horario).containsExactly("06:00");
	}

	// --- atualizar ---

	@Test
	void atualizarMantemCamposOmitidos() {
		RotaAgendadaResponse criado = service.criar(ANA, rotaDaAna,
				pedido("07:00", 15, List.of("SEGUNDA"), false));

		RotaAgendadaResponse atualizado = service.atualizar(ANA, criado.id(),
				pedido("07:30", null, List.of("SEGUNDA", "TERCA"), null));

		assertThat(atualizado.id()).isEqualTo(criado.id());
		assertThat(atualizado.horario()).isEqualTo("07:30");
		assertThat(atualizado.antecedenciaMinutos()).isEqualTo(15);
		assertThat(atualizado.ativa()).isFalse();
		assertThat(atualizado.diasSemana()).containsExactly(DiaSemana.SEGUNDA, DiaSemana.TERCA);
		assertThat(atualizado.criadoEm()).isEqualTo(criado.criadoEm());
	}

	@Test
	void atualizarPodePausarEReativar() {
		RotaAgendadaResponse criado = agendarDaAna("07:00", "SEGUNDA");

		assertThat(service.atualizar(ANA, criado.id(), pedido("07:00", null, List.of("SEGUNDA"), false)).ativa())
				.isFalse();
		assertThat(service.atualizar(ANA, criado.id(), pedido("07:00", null, List.of("SEGUNDA"), true)).ativa())
				.isTrue();
	}

	// --- acesso de outro usuário ---

	@Test
	void outroUsuarioNaoLeNemAlteraNemExclui() {
		RotaAgendadaResponse criado = agendarDaAna("07:00", "SEGUNDA");

		assertThatThrownBy(() -> service.buscar(BRUNO, criado.id()))
				.isInstanceOf(RecursoNaoEncontradoException.class);
		assertThatThrownBy(() -> service.atualizar(BRUNO, criado.id(), pedido("08:00", null, List.of("TERCA"), null)))
				.isInstanceOf(RecursoNaoEncontradoException.class);
		assertThatThrownBy(() -> service.excluir(BRUNO, criado.id()))
				.isInstanceOf(RecursoNaoEncontradoException.class);

		assertThat(service.buscar(ANA, criado.id()).horario()).isEqualTo("07:00");
	}

	// --- excluir ---

	@Test
	void excluirRemoveSoOAgendamentoIndicado() {
		RotaAgendadaResponse manha = agendarDaAna("07:00", "SEGUNDA");
		agendarDaAna("18:00", "SEGUNDA");

		service.excluir(ANA, manha.id());

		assertThat(service.listarDaRota(ANA, rotaDaAna)).extracting(RotaAgendadaResponse::horario)
				.containsExactly("18:00");
		assertThatThrownBy(() -> service.buscar(ANA, manha.id())).isInstanceOf(RecursoNaoEncontradoException.class);
	}

}
