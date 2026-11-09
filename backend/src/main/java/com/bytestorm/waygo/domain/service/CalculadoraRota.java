package com.bytestorm.waygo.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import com.bytestorm.waygo.domain.valueobject.ArestaRede;
import com.bytestorm.waygo.domain.valueobject.CaminhoRota;
import com.bytestorm.waygo.domain.valueobject.GrafoRede;
import com.bytestorm.waygo.domain.valueobject.NoRede;
import com.bytestorm.waygo.domain.valueobject.ParametrosRota;

/**
 * Calcula a melhor rota entre duas estações com Dijkstra. A busca parte de todos os nós da
 * estação de origem com custo zero e para no primeiro nó da estação de destino, assim não há
 * baldeação na estação de embarque nem na de desembarque. Linhas com circulação bloqueada
 * pelo status são ignoradas.
 */
public class CalculadoraRota {

	private record Visita(Long linhaEstacaoId, long custo) {
	}

	public Optional<CaminhoRota> calcular(GrafoRede grafo, Long origemEstacaoId, Long destinoEstacaoId,
			ParametrosRota parametros) {
		Map<Long, Long> custos = new HashMap<>();
		Map<Long, ArestaRede> chegadas = new HashMap<>();
		Set<Long> visitados = new HashSet<>();
		PriorityQueue<Visita> fila = new PriorityQueue<>(Comparator.comparingLong(Visita::custo));

		for (NoRede no : grafo.nosDaEstacao(origemEstacaoId)) {
			if (parametros.linhaDisponivel(no.linhaId())) {
				custos.put(no.linhaEstacaoId(), 0L);
				fila.add(new Visita(no.linhaEstacaoId(), 0L));
			}
		}

		while (!fila.isEmpty()) {
			Visita atual = fila.poll();
			if (!visitados.add(atual.linhaEstacaoId())) {
				continue;
			}
			NoRede no = grafo.no(atual.linhaEstacaoId());
			if (no.estacaoId().equals(destinoEstacaoId)) {
				return Optional.of(reconstruir(grafo, no, chegadas));
			}

			for (ArestaRede aresta : grafo.arestasDe(no.linhaEstacaoId())) {
				NoRede vizinho = grafo.no(aresta.destino());
				if (visitados.contains(vizinho.linhaEstacaoId()) || !parametros.linhaDisponivel(vizinho.linhaId())) {
					continue;
				}
				long custo = atual.custo() + parametros.custo(aresta, no);
				if (custo < custos.getOrDefault(vizinho.linhaEstacaoId(), Long.MAX_VALUE)) {
					custos.put(vizinho.linhaEstacaoId(), custo);
					chegadas.put(vizinho.linhaEstacaoId(), aresta);
					fila.add(new Visita(vizinho.linhaEstacaoId(), custo));
				}
			}
		}
		return Optional.empty();
	}

	private static CaminhoRota reconstruir(GrafoRede grafo, NoRede destino, Map<Long, ArestaRede> chegadas) {
		List<NoRede> nos = new ArrayList<>();
		List<ArestaRede> arestas = new ArrayList<>();
		NoRede atual = destino;
		nos.add(atual);
		ArestaRede chegada;
		while ((chegada = chegadas.get(atual.linhaEstacaoId())) != null) {
			arestas.add(chegada);
			atual = grafo.no(chegada.origem());
			nos.add(atual);
		}
		Collections.reverse(nos);
		Collections.reverse(arestas);
		return new CaminhoRota(nos, arestas);
	}

}
