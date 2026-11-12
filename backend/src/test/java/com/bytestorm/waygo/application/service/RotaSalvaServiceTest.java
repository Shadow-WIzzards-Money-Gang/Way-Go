package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.request.RotaSalvaRequest;
import com.bytestorm.waygo.application.dto.response.RotaSalvaResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;

/** Só o banco é simulado; as regras do service e do domínio são as reais. */
class RotaSalvaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-12T12:00:00Z");
	private static final UUID ANA = UUID.randomUUID();
	private static final UUID BRUNO = UUID.randomUUID();
	private static final Long SE = 1L;
	private static final Long LUZ = 2L;
	private static final Long BRAS = 3L;

	private final RotasEmMemoria rotas = new RotasEmMemoria();
	private final RotaSalvaService service = new RotaSalvaService(rotas, new EstacoesEmMemoria(),
			Clock.fixed(AGORA, ZoneOffset.UTC), CriterioRota.EQUILIBRADO);

	private static class RotasEmMemoria implements RotaSalvaRepository {

		private final Map<Long, RotaSalva> rotas = new LinkedHashMap<>();
		private long sequencia;

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
			return rotas.values().stream()
					.filter(rota -> rota.getIdUsuario().equals(idUsuario))
					.sorted(Comparator.comparing(RotaSalva::getCriadoEm).reversed())
					.toList();
		}

		@Override
		public void excluir(Long id) {
			rotas.remove(id);
		}
	}

	private static class EstacoesEmMemoria implements EstacaoRepository {

		private final Map<Long, Estacao> estacoes = Map.of(
				SE, Estacao.reconstruir(SE, "Sé", null, true),
				LUZ, Estacao.reconstruir(LUZ, "Luz", null, true),
				BRAS, Estacao.reconstruir(BRAS, "Brás", null, true));

		@Override
		public List<Estacao> listarTodas() {
			return new ArrayList<>(estacoes.values());
		}

		@Override
		public Optional<Estacao> buscarPorId(Long id) {
			return Optional.ofNullable(estacoes.get(id));
		}

		@Override
		public List<Estacao> buscarPorIds(Collection<Long> ids) {
			return ids.stream().map(estacoes::get).filter(e -> e != null).toList();
		}

		@Override
		public boolean existePorId(Long id) {
			return estacoes.containsKey(id);
		}
	}

	private RotaSalvaResponse salvarDaAna(String nome, Long origem, Long destino) {
		return service.salvar(ANA, new RotaSalvaRequest(nome, origem, destino, null));
	}

	// --- salvar ---

	@Test
	void salvarVinculaAoUsuarioEUsaCriterioPadrao() {
		RotaSalvaResponse resposta = salvarDaAna("Casa → Trabalho", SE, LUZ);

		assertThat(resposta.id()).isNotNull();
		assertThat(resposta.nome()).isEqualTo("Casa → Trabalho");
		assertThat(resposta.origem().nome()).isEqualTo("Sé");
		assertThat(resposta.destino().nome()).isEqualTo("Luz");
		assertThat(resposta.criterio()).isEqualTo(CriterioRota.EQUILIBRADO);
		assertThat(resposta.criadoEm()).isEqualTo(LocalDateTime.of(2026, 11, 12, 12, 0));
		assertThat(rotas.buscarPorId(resposta.id()).orElseThrow().getIdUsuario()).isEqualTo(ANA);
	}

	@Test
	void salvarAceitaCriterioInformado() {
		RotaSalvaResponse resposta = service.salvar(ANA, new RotaSalvaRequest("Rápida", SE, LUZ, "menor tempo"));

		assertThat(resposta.criterio()).isEqualTo(CriterioRota.MENOR_TEMPO);
	}

	@Test
	void salvarComCriterioInvalidoEhRequisicaoInvalida() {
		assertThatThrownBy(() -> service.salvar(ANA, new RotaSalvaRequest("X", SE, LUZ, "mais bonita")))
				.isInstanceOf(RequisicaoInvalidaException.class)
				.hasMessageContaining("MENOR_TEMPO");
	}

	@Test
	void salvarComEstacaoInexistenteEhNaoEncontrado() {
		assertThatThrownBy(() -> salvarDaAna("X", SE, 99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessageContaining("99");
	}

	@Test
	void salvarComOrigemIgualAoDestinoEhRequisicaoInvalida() {
		assertThatThrownBy(() -> salvarDaAna("X", SE, SE))
				.isInstanceOf(RequisicaoInvalidaException.class);
	}

	// --- listar e buscar ---

	@Test
	void listarTrazSoAsRotasDoUsuario() {
		salvarDaAna("Ida", SE, LUZ);
		service.salvar(BRUNO, new RotaSalvaRequest("Do Bruno", LUZ, BRAS, null));

		assertThat(service.listar(ANA)).extracting(RotaSalvaResponse::nome).containsExactly("Ida");
		assertThat(service.listar(BRUNO)).extracting(RotaSalvaResponse::nome).containsExactly("Do Bruno");
	}

	@Test
	void buscarRotaDeOutroUsuarioEhNaoEncontrado() {
		Long id = salvarDaAna("Ida", SE, LUZ).id();

		assertThat(service.buscar(ANA, id).nome()).isEqualTo("Ida");
		assertThatThrownBy(() -> service.buscar(BRUNO, id)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	// --- atualizar ---

	@Test
	void atualizarAlteraNomeEstacoesECriterioMantendoDonoEData() {
		Long id = salvarDaAna("Ida", SE, LUZ).id();

		RotaSalvaResponse resposta = service.atualizar(ANA, id,
				new RotaSalvaRequest("Nova", LUZ, BRAS, "MENOS_BALDEACOES"));

		assertThat(resposta.id()).isEqualTo(id);
		assertThat(resposta.nome()).isEqualTo("Nova");
		assertThat(resposta.origem().id()).isEqualTo(LUZ);
		assertThat(resposta.destino().id()).isEqualTo(BRAS);
		assertThat(resposta.criterio()).isEqualTo(CriterioRota.MENOS_BALDEACOES);
		assertThat(resposta.criadoEm()).isEqualTo(LocalDateTime.of(2026, 11, 12, 12, 0));
		assertThat(rotas.buscarPorId(id).orElseThrow().getIdUsuario()).isEqualTo(ANA);
	}

	@Test
	void atualizarPermiteInverterOrigemEDestino() {
		Long id = salvarDaAna("Ida", SE, LUZ).id();

		RotaSalvaResponse resposta = service.atualizar(ANA, id, new RotaSalvaRequest("Volta", LUZ, SE, null));

		assertThat(resposta.origem().id()).isEqualTo(LUZ);
		assertThat(resposta.destino().id()).isEqualTo(SE);
	}

	@Test
	void atualizarRotaDeOutroUsuarioEhNaoEncontrado() {
		Long id = salvarDaAna("Ida", SE, LUZ).id();

		assertThatThrownBy(() -> service.atualizar(BRUNO, id, new RotaSalvaRequest("Minha", SE, BRAS, null)))
				.isInstanceOf(RecursoNaoEncontradoException.class);
		assertThat(rotas.buscarPorId(id).orElseThrow().getNome()).isEqualTo("Ida");
	}

	// --- excluir ---

	@Test
	void excluirRemoveARota() {
		Long id = salvarDaAna("Ida", SE, LUZ).id();

		service.excluir(ANA, id);

		assertThat(rotas.buscarPorId(id)).isEmpty();
	}

	@Test
	void excluirRotaDeOutroUsuarioEhNaoEncontradoENaoRemove() {
		Long id = salvarDaAna("Ida", SE, LUZ).id();

		assertThatThrownBy(() -> service.excluir(BRUNO, id)).isInstanceOf(RecursoNaoEncontradoException.class);
		assertThat(rotas.buscarPorId(id)).isPresent();
	}

}
