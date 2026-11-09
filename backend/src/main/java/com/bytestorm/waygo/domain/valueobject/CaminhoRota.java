package com.bytestorm.waygo.domain.valueobject;

import java.util.List;

public record CaminhoRota(List<NoRede> nos, List<ArestaRede> arestas) {

	public CaminhoRota {
		nos = List.copyOf(nos);
		arestas = List.copyOf(arestas);
		if (nos.isEmpty() || arestas.size() != nos.size() - 1) {
			throw new IllegalArgumentException("caminho deve ter uma aresta a menos que a quantidade de nós");
		}
	}

}
