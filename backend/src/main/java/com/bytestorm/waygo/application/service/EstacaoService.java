package com.bytestorm.waygo.application.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.response.EstacaoDetalheResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDaEstacaoResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstacaoService {

	private final EstacaoRepository estacaoRepository;
	private final LinhaEstacaoRepository linhaEstacaoRepository;
	private final LinhaTransporteRepository linhaRepository;

	public List<EstacaoResumoResponse> listar() {
		return estacaoRepository.listarTodas().stream()
				.map(EstacaoResumoResponse::de)
				.toList();
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
