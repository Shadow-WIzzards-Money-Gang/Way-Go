package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.dto.request.RotaSalvaRequest;
import com.bytestorm.waygo.application.dto.response.RotaSalvaResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;

@Service
public class RotaSalvaService {

	private final RotaSalvaRepository rotaSalvaRepository;
	private final EstacaoRepository estacaoRepository;
	private final Clock clock;
	private final CriterioRota criterioPadrao;

	public RotaSalvaService(RotaSalvaRepository rotaSalvaRepository, EstacaoRepository estacaoRepository, Clock clock,
			@Value("${rotas.criterio-padrao:EQUILIBRADO}") CriterioRota criterioPadrao) {
		this.rotaSalvaRepository = rotaSalvaRepository;
		this.estacaoRepository = estacaoRepository;
		this.clock = clock;
		this.criterioPadrao = criterioPadrao;
	}

	public RotaSalvaResponse salvar(UUID idUsuario, RotaSalvaRequest request) {
		Estacao origem = buscarEstacao(request.idEstacaoOrigem());
		Estacao destino = buscarEstacao(request.idEstacaoDestino());
		CriterioRota criterio = resolverCriterio(request.criterio());

		RotaSalva rota = validando(() -> RotaSalva.criar(origem.getId(), destino.getId(), request.nome(), criterio,
				idUsuario, LocalDateTime.now(clock)));
		return RotaSalvaResponse.de(rotaSalvaRepository.salvar(rota), origem, destino);
	}

	public List<RotaSalvaResponse> listar(UUID idUsuario) {
		List<RotaSalva> rotas = rotaSalvaRepository.listarPorUsuario(idUsuario);
		List<Long> idsEstacoes = rotas.stream()
				.flatMap(rota -> Stream.of(rota.getIdEstacaoOrigem(), rota.getIdEstacaoDestino()))
				.distinct()
				.toList();
		Map<Long, Estacao> estacoes = estacaoRepository.buscarPorIds(idsEstacoes).stream()
				.collect(Collectors.toMap(Estacao::getId, Function.identity()));

		return rotas.stream()
				.map(rota -> RotaSalvaResponse.de(rota, estacoes.get(rota.getIdEstacaoOrigem()),
						estacoes.get(rota.getIdEstacaoDestino())))
				.toList();
	}

	public RotaSalvaResponse buscar(UUID idUsuario, Long id) {
		return paraResposta(buscarRotaDoUsuario(idUsuario, id));
	}

	public RotaSalvaResponse atualizar(UUID idUsuario, Long id, RotaSalvaRequest request) {
		RotaSalva atual = buscarRotaDoUsuario(idUsuario, id);
		Estacao origem = buscarEstacao(request.idEstacaoOrigem());
		Estacao destino = buscarEstacao(request.idEstacaoDestino());
		CriterioRota criterio = resolverCriterio(request.criterio());

		RotaSalva atualizada = validando(() -> RotaSalva.reconstruir(atual.getId(), origem.getId(), destino.getId(),
				request.nome(), criterio, atual.getIdUsuario(), atual.getCriadoEm()));
		return RotaSalvaResponse.de(rotaSalvaRepository.salvar(atualizada), origem, destino);
	}

	public void excluir(UUID idUsuario, Long id) {
		RotaSalva rota = buscarRotaDoUsuario(idUsuario, id);
		rotaSalvaRepository.excluir(rota.getId());
	}

	private RotaSalva buscarRotaDoUsuario(UUID idUsuario, Long id) {
		return rotaSalvaRepository.buscarPorIdEUsuario(id, idUsuario)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Rota salva não encontrada: " + id));
	}

	private RotaSalvaResponse paraResposta(RotaSalva rota) {
		return RotaSalvaResponse.de(rota, buscarEstacao(rota.getIdEstacaoOrigem()),
				buscarEstacao(rota.getIdEstacaoDestino()));
	}

	private Estacao buscarEstacao(Long id) {
		return estacaoRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estação não encontrada: " + id));
	}

	private CriterioRota resolverCriterio(String criterio) {
		if (criterio == null || criterio.isBlank()) {
			return criterioPadrao;
		}
		try {
			return CriterioRota.de(criterio);
		} catch (IllegalArgumentException e) {
			throw new RequisicaoInvalidaException(e.getMessage() + ". Valores aceitos: MENOR_TEMPO, "
					+ "MENOS_BALDEACOES ou EQUILIBRADO");
		}
	}

	private static RotaSalva validando(Supplier<RotaSalva> construcao) {
		try {
			return construcao.get();
		} catch (IllegalArgumentException e) {
			throw new RequisicaoInvalidaException(e.getMessage());
		}
	}

}
