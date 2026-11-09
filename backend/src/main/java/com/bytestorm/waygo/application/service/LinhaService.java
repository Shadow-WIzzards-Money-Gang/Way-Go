package com.bytestorm.waygo.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.response.EstacaoDaLinhaResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDetalheResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaStatusResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinhaService {

	private final LinhaTransporteRepository linhaRepository;
	private final LinhaEstacaoRepository linhaEstacaoRepository;
	private final EstacaoRepository estacaoRepository;
	private final LinhaStatusRepository linhaStatusRepository;

	public List<LinhaResumoResponse> listar() {
		return linhaRepository.listarTodas().stream()
				.map(LinhaResumoResponse::de)
				.toList();
	}

	public LinhaDetalheResponse buscarPorId(Long id) {
		LinhaTransporte linha = linhaRepository.buscarPorId(id)
				.orElseThrow(() -> linhaNaoEncontrada(id));
		return LinhaDetalheResponse.de(linha, montarEstacoes(id));
	}

	public List<EstacaoDaLinhaResponse> listarEstacoes(Long id) {
		if (!linhaRepository.existePorId(id)) {
			throw linhaNaoEncontrada(id);
		}
		return montarEstacoes(id);
	}

	public LinhaStatusResponse buscarStatusAtual(Long id) {
		if (!linhaRepository.existePorId(id)) {
			throw linhaNaoEncontrada(id);
		}
		return linhaStatusRepository.buscarAtualPorLinha(id)
				.map(LinhaStatusResponse::de)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum status registrado para a linha " + id));
	}

	private List<EstacaoDaLinhaResponse> montarEstacoes(Long linhaId) {
		List<LinhaEstacao> linhaEstacoes = linhaEstacaoRepository.listarPorLinhaOrdenadoPorSequencia(linhaId);
		Map<Long, Estacao> estacoesPorId = estacaoRepository
				.buscarPorIds(linhaEstacoes.stream().map(LinhaEstacao::getEstacaoId).toList())
				.stream()
				.collect(Collectors.toMap(Estacao::getId, Function.identity()));

		return linhaEstacoes.stream()
				.filter(linhaEstacao -> estacoesPorId.containsKey(linhaEstacao.getEstacaoId()))
				.map(linhaEstacao -> EstacaoDaLinhaResponse.de(linhaEstacao,
						estacoesPorId.get(linhaEstacao.getEstacaoId())))
				.toList();
	}

	private static RecursoNaoEncontradoException linhaNaoEncontrada(Long id) {
		return new RecursoNaoEncontradoException("Linha não encontrada: " + id);
	}

}
