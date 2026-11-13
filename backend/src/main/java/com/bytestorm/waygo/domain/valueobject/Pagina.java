package com.bytestorm.waygo.domain.valueobject;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> itens, int numero, int tamanho, long totalItens) {

	public Pagina {
		itens = List.copyOf(itens);
	}

	public int totalPaginas() {
		return tamanho == 0 ? 0 : (int) Math.ceil((double) totalItens / tamanho);
	}

	public boolean primeira() {
		return numero == 0;
	}

	public boolean ultima() {
		return numero + 1 >= totalPaginas();
	}

	public <R> Pagina<R> map(Function<? super T, ? extends R> conversor) {
		List<R> convertidos = itens.stream().<R>map(conversor).toList();
		return new Pagina<>(convertidos, numero, tamanho, totalItens);
	}

}
