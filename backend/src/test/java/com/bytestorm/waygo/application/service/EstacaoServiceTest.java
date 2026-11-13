package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.util.Textos;
import com.bytestorm.waygo.domain.valueobject.Pagina;

/** O banco é simulado em memória; a filtragem real (TRANSLATE/LIKE) fica na query JPQL. */
class EstacaoServiceTest {

	private final EstacoesEmMemoria estacoes = new EstacoesEmMemoria(List.of(
			Estacao.reconstruir(1L, "Brás", null, true),
			Estacao.reconstruir(2L, "Luz", null, true),
			Estacao.reconstruir(3L, "Sé", null, true),
			Estacao.reconstruir(4L, "Tatuapé", null, true),
			Estacao.reconstruir(5L, "República", null, true)));

	private final EstacaoService service = new EstacaoService(estacoes, null, null);

	private static class EstacoesEmMemoria implements EstacaoRepository {

		private final List<Estacao> todas;
		String ultimaBusca;
		boolean ultimoSomenteIntegracoes;

		EstacoesEmMemoria(List<Estacao> todas) {
			this.todas = todas;
		}

		@Override
		public List<Estacao> listarTodas() {
			return todas;
		}

		@Override
		public Pagina<Estacao> listarPaginado(String busca, boolean somenteIntegracoes, int pagina, int tamanho) {
			ultimaBusca = busca;
			ultimoSomenteIntegracoes = somenteIntegracoes;
			List<Estacao> filtradas = todas.stream()
					.filter(e -> Textos.normalizar(e.getNome()).contains(busca))
					.toList();
			int inicio = Math.min(pagina * tamanho, filtradas.size());
			int fim = Math.min(inicio + tamanho, filtradas.size());
			return new Pagina<>(filtradas.subList(inicio, fim), pagina, tamanho, filtradas.size());
		}

		@Override
		public Optional<Estacao> buscarPorId(Long id) {
			return todas.stream().filter(e -> e.getId().equals(id)).findFirst();
		}

		@Override
		public List<Estacao> buscarPorIds(Collection<Long> ids) {
			return todas.stream().filter(e -> ids.contains(e.getId())).toList();
		}

		@Override
		public boolean existePorId(Long id) {
			return buscarPorId(id).isPresent();
		}

		@Override
		public Estacao salvar(Estacao estacao) {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	void listarDevolveMetadadosDaPagina() {
		PaginaResponse<EstacaoResumoResponse> primeira = service.listar(0, 2, null, false);

		assertThat(primeira.conteudo()).extracting(EstacaoResumoResponse::nome).containsExactly("Brás", "Luz");
		assertThat(primeira.pagina()).isZero();
		assertThat(primeira.tamanho()).isEqualTo(2);
		assertThat(primeira.totalItens()).isEqualTo(5);
		assertThat(primeira.totalPaginas()).isEqualTo(3);
		assertThat(primeira.primeira()).isTrue();
		assertThat(primeira.ultima()).isFalse();
	}

	@Test
	void ultimaPaginaPodeVirIncompleta() {
		PaginaResponse<EstacaoResumoResponse> ultima = service.listar(2, 2, null, false);

		assertThat(ultima.conteudo()).hasSize(1);
		assertThat(ultima.primeira()).isFalse();
		assertThat(ultima.ultima()).isTrue();
	}

	@Test
	void paginaAlemDoFimVemVazia() {
		PaginaResponse<EstacaoResumoResponse> resposta = service.listar(10, 2, null, false);

		assertThat(resposta.conteudo()).isEmpty();
		assertThat(resposta.ultima()).isTrue();
	}

	@Test
	void buscaChegaNormalizadaESemCuringasAoRepositorio() {
		service.listar(0, 20, "  TATUAPÉ%_ ", true);

		assertThat(estacoes.ultimaBusca).isEqualTo("tatuape");
		assertThat(estacoes.ultimoSomenteIntegracoes).isTrue();
	}

	@Test
	void semResultadosNaoTemPaginas() {
		PaginaResponse<EstacaoResumoResponse> resposta = service.listar(0, 20, "xyz", false);

		assertThat(resposta.totalItens()).isZero();
		assertThat(resposta.totalPaginas()).isZero();
		assertThat(resposta.ultima()).isTrue();
	}

	@Test
	void rejeitaParametrosDePaginacaoInvalidos() {
		assertThatThrownBy(() -> service.listar(-1, 20, null, false))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.listar(0, 0, null, false))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.listar(0, EstacaoService.TAMANHO_MAXIMO + 1, null, false))
				.isInstanceOf(RequisicaoInvalidaException.class);
	}

}
