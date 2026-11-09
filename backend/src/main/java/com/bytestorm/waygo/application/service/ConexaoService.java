package com.bytestorm.waygo.application.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.response.ConexaoResponse;
import com.bytestorm.waygo.application.dto.response.PontoConexaoResponse;
import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.interfaces.BaldeacaoRepository;
import com.bytestorm.waygo.domain.interfaces.ConexaoEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConexaoService {

	private static final Comparator<ConexaoResponse> ORDEM = Comparator
			.comparing(ConexaoResponse::tipo)
			.thenComparing(conexao -> conexao.origem().linhaId())
			.thenComparing(conexao -> conexao.destino().linhaId())
			.thenComparing(conexao -> conexao.destino().sequencia());

	private final EstacaoRepository estacaoRepository;
	private final LinhaEstacaoRepository linhaEstacaoRepository;
	private final LinhaTransporteRepository linhaRepository;
	private final ConexaoEstacaoRepository conexaoEstacaoRepository;
	private final BaldeacaoRepository baldeacaoRepository;

	/**
	 * Lista as conexões (mesma linha) e baldeações (entre linhas) que partem da estação.
	 * Como a rede é cadastrada nos dois sentidos, considerar só a origem evita duplicar cada ligação.
	 */
	public List<ConexaoResponse> listarPorEstacao(Long estacaoId) {
		if (!estacaoRepository.existePorId(estacaoId)) {
			throw new RecursoNaoEncontradoException("Estação não encontrada: " + estacaoId);
		}

		List<LinhaEstacao> origens = linhaEstacaoRepository.listarPorEstacao(estacaoId);
		if (origens.isEmpty()) {
			return List.of();
		}
		List<Long> origemIds = origens.stream().map(LinhaEstacao::getId).toList();

		List<ConexaoEstacao> conexoes = conexaoEstacaoRepository.listarPorOrigens(origemIds);
		List<Baldeacao> baldeacoes = baldeacaoRepository.listarPorOrigens(origemIds);

		Map<Long, LinhaEstacao> linhaEstacoes = new HashMap<>(
				origens.stream().collect(Collectors.toMap(LinhaEstacao::getId, Function.identity())));
		List<Long> destinoIds = Stream.concat(
				conexoes.stream().map(ConexaoEstacao::getLinhaEstacaoDestinoId),
				baldeacoes.stream().map(Baldeacao::getLinhaEstacaoDestinoId))
				.filter(id -> !linhaEstacoes.containsKey(id))
				.distinct()
				.toList();
		linhaEstacaoRepository.buscarPorIds(destinoIds)
				.forEach(linhaEstacao -> linhaEstacoes.put(linhaEstacao.getId(), linhaEstacao));

		Map<Long, Estacao> estacoes = estacaoRepository
				.buscarPorIds(linhaEstacoes.values().stream().map(LinhaEstacao::getEstacaoId).distinct().toList())
				.stream()
				.collect(Collectors.toMap(Estacao::getId, Function.identity()));
		Map<Long, LinhaTransporte> linhas = linhaRepository
				.buscarPorIds(linhaEstacoes.values().stream().map(LinhaEstacao::getLinhaId).distinct().toList())
				.stream()
				.collect(Collectors.toMap(LinhaTransporte::getId, Function.identity()));

		Function<Long, PontoConexaoResponse> ponto = linhaEstacaoId -> {
			LinhaEstacao linhaEstacao = linhaEstacoes.get(linhaEstacaoId);
			return PontoConexaoResponse.de(linhaEstacao, estacoes.get(linhaEstacao.getEstacaoId()),
					linhas.get(linhaEstacao.getLinhaId()));
		};

		return Stream.concat(
				conexoes.stream().map(conexao -> ConexaoResponse.de(conexao,
						ponto.apply(conexao.getLinhaEstacaoOrigemId()),
						ponto.apply(conexao.getLinhaEstacaoDestinoId()))),
				baldeacoes.stream().map(baldeacao -> ConexaoResponse.de(baldeacao,
						ponto.apply(baldeacao.getLinhaEstacaoOrigemId()),
						ponto.apply(baldeacao.getLinhaEstacaoDestinoId()))))
				.sorted(ORDEM)
				.toList();
	}

}
