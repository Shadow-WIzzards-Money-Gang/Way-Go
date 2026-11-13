package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.cache.GrafoRedeCache;
import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.application.dto.request.LinhaAtualizacaoRequest;
import com.bytestorm.waygo.application.dto.request.LinhaRequest;
import com.bytestorm.waygo.application.dto.request.OrdemEstacoesRequest;
import com.bytestorm.waygo.application.dto.request.StatusLinhaRequest;
import com.bytestorm.waygo.application.dto.response.EstacaoDaLinhaResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaStatusResponse;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.exception.ConflitoException;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminLinhaService {

	private final LinhaTransporteRepository linhaRepository;
	private final LinhaEstacaoRepository linhaEstacaoRepository;
	private final LinhaStatusRepository linhaStatusRepository;
	private final LinhaService linhaService;
	private final GrafoRedeCache grafoCache;
	private final StatusLinhasCache statusCache;
	private final Clock clock;

	public LinhaResumoResponse criar(LinhaRequest request) {
		if (linhaRepository.existePorCodigo(request.codigo())) {
			throw new ConflitoException("Já existe uma linha com o código " + request.codigo().trim());
		}
		LinhaTransporte linha = validando(() -> LinhaTransporte.criar(request.nome(), request.codigo(),
				request.cor(), request.operadora(), request.tipoTransporte()));
		LinhaTransporte salva = linhaRepository.salvar(linha);
		grafoCache.invalidar();
		// O mapa código -> id do cache de status precisa conhecer a linha nova.
		statusCache.invalidar();
		return LinhaResumoResponse.de(salva);
	}

	public LinhaResumoResponse atualizar(Long id, LinhaAtualizacaoRequest request) {
		LinhaTransporte linha = buscar(id);
		validando(() -> {
			if (request.nome() != null) {
				linha.renomear(request.nome());
			}
			if (request.cor() != null) {
				linha.alterarCor(request.cor());
			}
			if (request.operadora() != null) {
				linha.alterarOperadora(request.operadora());
			}
			if (Boolean.TRUE.equals(request.ativa())) {
				linha.ativar();
			} else if (Boolean.FALSE.equals(request.ativa())) {
				linha.desativar();
			}
			return linha;
		});
		return salvar(linha);
	}

	/** "Excluir" uma linha é desativá-la: ela é referenciada por estações, conexões e rotas salvas. */
	public void desativar(Long id) {
		LinhaTransporte linha = buscar(id);
		linha.desativar();
		salvar(linha);
	}

	public List<EstacaoDaLinhaResponse> reordenarEstacoes(Long id, OrdemEstacoesRequest request) {
		buscar(id);
		List<LinhaEstacao> atuais = linhaEstacaoRepository.listarPorLinhaOrdenadoPorSequencia(id);
		List<Long> novaOrdem = request.idsEstacoes();
		Map<Long, LinhaEstacao> porEstacao = atuais.stream()
				.collect(Collectors.toMap(LinhaEstacao::getEstacaoId, Function.identity()));

		boolean mesmasEstacoes = novaOrdem.size() == atuais.size()
				&& new HashSet<>(novaOrdem).size() == novaOrdem.size()
				&& porEstacao.keySet().containsAll(novaOrdem);
		if (!mesmasEstacoes) {
			throw new RequisicaoInvalidaException("Informe todas as " + atuais.size()
					+ " estações da linha, cada uma uma única vez");
		}

		for (int i = 0; i < novaOrdem.size(); i++) {
			porEstacao.get(novaOrdem.get(i)).reposicionar(i + 1);
		}
		linhaEstacaoRepository.salvarSequencias(atuais);
		grafoCache.invalidar();
		return linhaService.listarEstacoes(id);
	}

	/** Status manual: prevalece sobre a API externa por um tempo (ver AtualizacaoStatusLinhaService). */
	public LinhaStatusResponse alterarStatus(Long id, StatusLinhaRequest request) {
		buscar(id);
		LinhaStatus status = validando(() -> LinhaStatus.registrarPeloAdministrador(id, request.status(),
				request.descricao(), LocalDateTime.now(clock)));
		LinhaStatus salvo = linhaStatusRepository.salvar(status);
		statusCache.registrar(id, salvo.getStatus());
		return LinhaStatusResponse.de(salvo);
	}

	private LinhaResumoResponse salvar(LinhaTransporte linha) {
		LinhaTransporte salva = linhaRepository.salvar(linha);
		grafoCache.invalidar();
		return LinhaResumoResponse.de(salva);
	}

	private LinhaTransporte buscar(Long id) {
		return linhaRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Linha não encontrada: " + id));
	}

	private static <T> T validando(Supplier<T> operacao) {
		try {
			return operacao.get();
		} catch (IllegalArgumentException | IllegalStateException ex) {
			throw new RequisicaoInvalidaException(ex.getMessage());
		}
	}

}
