package com.bytestorm.waygo.application.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.cache.GrafoRedeCache;
import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.application.dto.request.RotaRequest;
import com.bytestorm.waygo.application.dto.response.BaldeacaoRotaResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.RotaResponse;
import com.bytestorm.waygo.application.dto.response.TrechoRotaResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoConexao;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.exception.RotaNaoEncontradaException;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.service.CalculadoraRota;
import com.bytestorm.waygo.domain.valueobject.ArestaRede;
import com.bytestorm.waygo.domain.valueobject.CaminhoRota;
import com.bytestorm.waygo.domain.valueobject.GrafoRede;
import com.bytestorm.waygo.domain.valueobject.NoRede;
import com.bytestorm.waygo.domain.valueobject.ParametrosRota;

@Service
public class RotaService {

	private final GrafoRedeCache grafoCache;
	private final StatusLinhasCache statusCache;
	private final EstacaoRepository estacaoRepository;
	private final CriterioRota criterioPadrao;
	private final Duration penalidadeBaldeacao;
	private final double fatorLentidao;
	private final CalculadoraRota calculadora = new CalculadoraRota();

	public RotaService(GrafoRedeCache grafoCache, StatusLinhasCache statusCache, EstacaoRepository estacaoRepository,
			@Value("${rotas.criterio-padrao:EQUILIBRADO}") CriterioRota criterioPadrao,
			@Value("${rotas.penalidade-baldeacao:PT3M}") Duration penalidadeBaldeacao,
			@Value("${rotas.fator-lentidao:1.5}") double fatorLentidao) {
		this.grafoCache = grafoCache;
		this.statusCache = statusCache;
		this.estacaoRepository = estacaoRepository;
		this.criterioPadrao = criterioPadrao;
		this.penalidadeBaldeacao = penalidadeBaldeacao;
		this.fatorLentidao = fatorLentidao;
	}

	public RotaResponse calcular(RotaRequest request) {
		CriterioRota criterio = resolverCriterio(request.criterio());

		GrafoRede grafo = grafoCache.obter();
		Estacao origem = buscarEstacao(grafo, request.origem());
		Estacao destino = buscarEstacao(grafo, request.destino());
		if (origem.getId().equals(destino.getId())) {
			throw new RequisicaoInvalidaException("A estação de origem e a de destino devem ser diferentes");
		}

		boolean alternativa = Boolean.TRUE.equals(request.alternativa());
		ParametrosRota parametros = new ParametrosRota(criterio, fatorLentidao,
				(int) penalidadeBaldeacao.toSeconds(), statusCache.statusAtuais(), alternativa);

		CaminhoRota caminho = calculadora.calcular(grafo, origem.getId(), destino.getId(), parametros)
				.orElseThrow(() -> new RotaNaoEncontradaException(alternativa
						? "Não há rota entre " + origem.getNome() + " e " + destino.getNome()
								+ " que evite linhas com problemas no momento"
						: "Não há rota disponível entre " + origem.getNome() + " e " + destino.getNome()
								+ " com o status atual das linhas"));
		return montarResposta(grafo, parametros, origem, destino, caminho);
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

	private Estacao buscarEstacao(GrafoRede grafo, String nome) {
		List<Estacao> encontradas = grafo.estacoesPorNome(nome);
		if (encontradas.size() == 1) {
			return encontradas.getFirst();
		}
		if (encontradas.size() > 1) {
			throw new RequisicaoInvalidaException("Há mais de uma estação com o nome \"" + nome.trim() + "\"");
		}

		boolean existe = estacaoRepository.listarTodas().stream()
				.anyMatch(estacao -> estacao.possuiNome(nome));
		throw existe
				? new RotaNaoEncontradaException(
						"A estação " + nome.trim() + " não é atendida por nenhuma linha em operação")
				: new RecursoNaoEncontradoException("Estação não encontrada: " + nome.trim());
	}

	private RotaResponse montarResposta(GrafoRede grafo, ParametrosRota parametros, Estacao origem, Estacao destino,
			CaminhoRota caminho) {
		List<NoRede> nos = caminho.nos();
		List<ArestaRede> arestas = caminho.arestas();
		List<TrechoRotaResponse> trechos = new ArrayList<>();
		List<BaldeacaoRotaResponse> baldeacoes = new ArrayList<>();

		int tempoTotal = 0;
		int distanciaTotal = 0;
		int inicioTrecho = 0;
		int tempoTrecho = 0;
		int distanciaTrecho = 0;
		NoRede inicioBaldeacao = null;
		int tempoBaldeacao = 0;

		for (int i = 0; i < arestas.size(); i++) {
			ArestaRede aresta = arestas.get(i);
			NoRede de = nos.get(i);
			int tempo = parametros.tempoEstimadoSegundos(aresta, de);
			tempoTotal += tempo;
			distanciaTotal += aresta.distanciaMetros();

			if (aresta.tipo() == TipoConexao.BALDEACAO) {
				if (inicioBaldeacao == null) {
					if (i > inicioTrecho) {
						trechos.add(montarTrecho(grafo, parametros, trechos.size() + 1, nos.subList(inicioTrecho, i + 1),
								tempoTrecho, distanciaTrecho));
					}
					inicioBaldeacao = de;
				}
				tempoBaldeacao += tempo;
				continue;
			}

			if (inicioBaldeacao != null) {
				baldeacoes.add(montarBaldeacao(grafo, inicioBaldeacao, de, tempoBaldeacao));
				inicioBaldeacao = null;
				tempoBaldeacao = 0;
				inicioTrecho = i;
				tempoTrecho = 0;
				distanciaTrecho = 0;
			}
			tempoTrecho += tempo;
			distanciaTrecho += aresta.distanciaMetros();
		}

		int ultimo = nos.size() - 1;
		if (inicioBaldeacao != null) {
			baldeacoes.add(montarBaldeacao(grafo, inicioBaldeacao, nos.get(ultimo), tempoBaldeacao));
		} else if (ultimo > inicioTrecho) {
			trechos.add(montarTrecho(grafo, parametros, trechos.size() + 1, nos.subList(inicioTrecho, nos.size()),
					tempoTrecho, distanciaTrecho));
		}

		return new RotaResponse(
				EstacaoResumoResponse.de(origem),
				EstacaoResumoResponse.de(destino),
				parametros.criterio(),
				parametros.somenteLinhasNormais(),
				tempoTotal,
				(int) Math.ceil(tempoTotal / 60.0),
				distanciaTotal,
				baldeacoes.size(),
				montarAvisos(trechos),
				trechos,
				baldeacoes);
	}

	private TrechoRotaResponse montarTrecho(GrafoRede grafo, ParametrosRota parametros, int ordem, List<NoRede> nos,
			int tempoSegundos, int distanciaMetros) {
		NoRede embarque = nos.getFirst();
		NoRede desembarque = nos.getLast();
		List<NoRede> nosDaLinha = grafo.nosDaLinha(embarque.linhaId());
		NoRede terminal = desembarque.sequencia() > embarque.sequencia() ? nosDaLinha.getLast() : nosDaLinha.getFirst();

		return new TrechoRotaResponse(
				ordem,
				linha(grafo, embarque),
				parametros.statusDa(embarque.linhaId()),
				estacao(grafo, embarque),
				estacao(grafo, desembarque),
				estacao(grafo, terminal),
				nos.stream().map(no -> estacao(grafo, no)).toList(),
				nos.size() - 1,
				tempoSegundos,
				distanciaMetros);
	}

	private BaldeacaoRotaResponse montarBaldeacao(GrafoRede grafo, NoRede de, NoRede para, int tempoSegundos) {
		return new BaldeacaoRotaResponse(estacao(grafo, de), estacao(grafo, para), linha(grafo, de), linha(grafo, para),
				tempoSegundos);
	}

	private List<String> montarAvisos(List<TrechoRotaResponse> trechos) {
		Set<String> avisos = new LinkedHashSet<>();
		for (TrechoRotaResponse trecho : trechos) {
			StatusOperacional status = trecho.statusLinha();
			if (status == StatusOperacional.NORMAL) {
				continue;
			}
			String aviso = trecho.linha().nome() + " está com status \"" + status.getDescricao() + "\"";
			avisos.add(status.afetaTempo() ? aviso + "; o tempo estimado do trecho foi ajustado" : aviso);
		}
		return List.copyOf(avisos);
	}

	private static LinhaResumoResponse linha(GrafoRede grafo, NoRede no) {
		LinhaTransporte linha = grafo.linha(no.linhaId()).orElseThrow();
		return LinhaResumoResponse.de(linha);
	}

	private static EstacaoResumoResponse estacao(GrafoRede grafo, NoRede no) {
		return EstacaoResumoResponse.de(grafo.estacao(no.estacaoId()).orElseThrow());
	}

}
