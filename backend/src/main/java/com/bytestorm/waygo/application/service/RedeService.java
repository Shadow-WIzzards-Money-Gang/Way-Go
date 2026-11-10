package com.bytestorm.waygo.application.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.cache.GrafoRedeCache;
import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.application.dto.response.RedeResponse;
import com.bytestorm.waygo.application.dto.response.RedeResponse.BaldeacaoRede;
import com.bytestorm.waygo.application.dto.response.RedeResponse.ConexaoRede;
import com.bytestorm.waygo.application.dto.response.RedeResponse.EstacaoRede;
import com.bytestorm.waygo.application.dto.response.RedeResponse.LinhaRede;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoConexao;
import com.bytestorm.waygo.domain.valueobject.ArestaRede;
import com.bytestorm.waygo.domain.valueobject.Coordenada;
import com.bytestorm.waygo.domain.valueobject.GrafoRede;
import com.bytestorm.waygo.domain.valueobject.NoRede;

@Service
public class RedeService {

	private final GrafoRedeCache grafoCache;
	private final StatusLinhasCache statusCache;

	public RedeService(GrafoRedeCache grafoCache, StatusLinhasCache statusCache) {
		this.grafoCache = grafoCache;
		this.statusCache = statusCache;
	}

	public RedeResponse obter() {
		GrafoRede grafo = grafoCache.obter();
		Map<Long, StatusOperacional> status = statusCache.statusAtuais();

		return new RedeResponse(montarEstacoes(grafo), montarLinhas(grafo, status), montarConexoes(grafo),
				montarBaldeacoes(grafo));
	}

	private List<EstacaoRede> montarEstacoes(GrafoRede grafo) {
		return grafo.estacoes().stream()
				.sorted(Comparator.comparing(Estacao::getId))
				.map(estacao -> {
					Coordenada coordenada = estacao.getCoordenada();
					List<Long> linhaIds = grafo.nosDaEstacao(estacao.getId()).stream()
							.map(NoRede::linhaId)
							.distinct()
							.sorted()
							.toList();
					return new EstacaoRede(estacao.getId(), estacao.getNome(),
							coordenada == null ? null : coordenada.latitude(),
							coordenada == null ? null : coordenada.longitude(),
							linhaIds);
				})
				.toList();
	}

	private List<LinhaRede> montarLinhas(GrafoRede grafo, Map<Long, StatusOperacional> statusPorLinha) {
		return grafo.linhas().stream()
				.sorted(Comparator.comparing(LinhaTransporte::getId))
				.map(linha -> {
					StatusOperacional status = statusPorLinha.getOrDefault(linha.getId(), StatusOperacional.NORMAL);
					List<Long> estacaoIds = grafo.nosDaLinha(linha.getId()).stream()
							.map(NoRede::estacaoId)
							.toList();
					return new LinhaRede(linha.getId(), linha.getNome(), linha.getCodigo(), linha.getCor(),
							linha.getOperadora(), linha.getTipoTransporte(), status, status.getDescricao(),
							!status.bloqueiaCirculacao(), estacaoIds);
				})
				.toList();
	}

	/** As conexões são armazenadas nos dois sentidos; para o mapa basta um segmento por par de estações na linha. */
	private List<ConexaoRede> montarConexoes(GrafoRede grafo) {
		Map<String, ConexaoRede> unicas = new LinkedHashMap<>();
		for (ArestaRede aresta : arestasOrdenadas(grafo, TipoConexao.CONEXAO)) {
			NoRede origem = grafo.no(aresta.origem());
			NoRede destino = grafo.no(aresta.destino());
			String chave = origem.linhaId() + ":" + chaveNaoDirecionada(origem.estacaoId(), destino.estacaoId());
			unicas.putIfAbsent(chave, new ConexaoRede(origem.linhaId(), origem.estacaoId(), destino.estacaoId(),
					aresta.tempoSegundos(), aresta.distanciaMetros()));
		}
		return List.copyOf(unicas.values());
	}

	private List<BaldeacaoRede> montarBaldeacoes(GrafoRede grafo) {
		Map<String, BaldeacaoRede> unicas = new LinkedHashMap<>();
		for (ArestaRede aresta : arestasOrdenadas(grafo, TipoConexao.BALDEACAO)) {
			NoRede origem = grafo.no(aresta.origem());
			NoRede destino = grafo.no(aresta.destino());
			String chave = chaveNaoDirecionada(origem.linhaEstacaoId(), destino.linhaEstacaoId());
			unicas.putIfAbsent(chave, new BaldeacaoRede(origem.estacaoId(), origem.linhaId(), destino.estacaoId(),
					destino.linhaId(), aresta.tempoSegundos()));
		}
		return List.copyOf(unicas.values());
	}

	private static List<ArestaRede> arestasOrdenadas(GrafoRede grafo, TipoConexao tipo) {
		return grafo.arestas().stream()
				.filter(aresta -> aresta.tipo() == tipo)
				.sorted(Comparator.comparing(ArestaRede::origem).thenComparing(ArestaRede::destino))
				.toList();
	}

	private static String chaveNaoDirecionada(Long a, Long b) {
		return a <= b ? a + "-" + b : b + "-" + a;
	}

}
