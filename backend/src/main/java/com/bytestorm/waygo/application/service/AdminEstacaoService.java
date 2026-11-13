package com.bytestorm.waygo.application.service;

import java.math.BigDecimal;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.cache.GrafoRedeCache;
import com.bytestorm.waygo.application.dto.request.EstacaoRequest;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.valueobject.Coordenada;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEstacaoService {

	private final EstacaoRepository estacaoRepository;
	private final GrafoRedeCache grafoCache;

	public EstacaoResumoResponse criar(EstacaoRequest request) {
		if (request.nome() == null || request.nome().isBlank()) {
			throw new RequisicaoInvalidaException("O nome da estação é obrigatório");
		}
		Estacao estacao = validando(() -> Estacao.criar(request.nome(),
				coordenada(request.latitude(), request.longitude())));
		return salvar(estacao);
	}

	public EstacaoResumoResponse atualizar(Long id, EstacaoRequest request) {
		Estacao estacao = buscar(id);
		validando(() -> {
			if (request.nome() != null) {
				estacao.renomear(request.nome());
			}
			if (request.latitude() != null || request.longitude() != null) {
				estacao.relocalizar(coordenada(request.latitude(), request.longitude()));
			}
			if (Boolean.TRUE.equals(request.ativa())) {
				estacao.ativar();
			} else if (Boolean.FALSE.equals(request.ativa())) {
				estacao.desativar();
			}
			return estacao;
		});
		return salvar(estacao);
	}

	/** "Excluir" uma estação é desativá-la: ela é referenciada pelas linhas e pelas rotas salvas. */
	public void desativar(Long id) {
		Estacao estacao = buscar(id);
		estacao.desativar();
		salvar(estacao);
	}

	private EstacaoResumoResponse salvar(Estacao estacao) {
		Estacao salva = estacaoRepository.salvar(estacao);
		grafoCache.invalidar();
		return EstacaoResumoResponse.de(salva);
	}

	private Estacao buscar(Long id) {
		return estacaoRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estação não encontrada: " + id));
	}

	/** Sem latitude nem longitude a estação fica sem posição; só uma das duas é inválido. */
	private static Coordenada coordenada(BigDecimal latitude, BigDecimal longitude) {
		if (latitude == null && longitude == null) {
			return null;
		}
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("Informe latitude e longitude juntas");
		}
		return new Coordenada(latitude, longitude);
	}

	private static <T> T validando(Supplier<T> operacao) {
		try {
			return operacao.get();
		} catch (IllegalArgumentException | IllegalStateException ex) {
			throw new RequisicaoInvalidaException(ex.getMessage());
		}
	}

}
