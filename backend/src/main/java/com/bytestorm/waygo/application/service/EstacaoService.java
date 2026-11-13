package com.bytestorm.waygo.application.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.response.EstacaoDetalheResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDaEstacaoResponse;
import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;
import com.bytestorm.waygo.domain.util.Textos;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstacaoService {

	private final EstacaoRepository estacaoRepository;
	private final LinhaEstacaoRepository linhaEstacaoRepository;
	private final LinhaTransporteRepository linhaRepository;

	public static final int TAMANHO_PADRAO = 20;
	public static final int TAMANHO_MAXIMO = 100;

	public PaginaResponse<EstacaoResumoResponse> listar(int pagina, int tamanho, String busca,
			boolean somenteIntegracoes) {
		if (pagina < 0) {
			throw new RequisicaoInvalidaException("O parâmetro 'pagina' deve ser maior ou igual a 0");
		}
		if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
			throw new RequisicaoInvalidaException(
					"O parâmetro 'tamanho' deve estar entre 1 e " + TAMANHO_MAXIMO);
		}
		String termo = Textos.normalizar(busca).replace("%", "").replace("_", "");
		return PaginaResponse.de(estacaoRepository
				.listarPaginado(termo, somenteIntegracoes, pagina, tamanho)
				.map(EstacaoResumoResponse::de));
	}

	public EstacaoDetalheResponse buscarPorId(Long id) {
		Estacao estacao = estacaoRepository.buscarPorId(id)
				.orElseThrow(() -> estacaoNaoEncontrada(id));
		return EstacaoDetalheResponse.de(estacao, montarLinhas(id));
	}

	public List<LinhaDaEstacaoResponse> listarLinhas(Long id) {
		if (!estacaoRepository.existePorId(id)) {
			throw estacaoNaoEncontrada(id);
		}
		return montarLinhas(id);
	}

	private List<LinhaDaEstacaoResponse> montarLinhas(Long estacaoId) {
		List<Long> linhaIds = linhaEstacaoRepository.listarPorEstacao(estacaoId).stream()
				.map(LinhaEstacao::getLinhaId)
				.distinct()
				.toList();

		return linhaRepository.buscarPorIds(linhaIds).stream()
				.sorted(Comparator.comparing(LinhaTransporte::getId))
				.map(LinhaDaEstacaoResponse::de)
				.toList();
	}

	private static RecursoNaoEncontradoException estacaoNaoEncontrada(Long id) {
		return new RecursoNaoEncontradoException("Estação não encontrada: " + id);
	}

}
