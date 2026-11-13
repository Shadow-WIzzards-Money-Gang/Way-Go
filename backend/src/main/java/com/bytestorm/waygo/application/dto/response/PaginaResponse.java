package com.bytestorm.waygo.application.dto.response;

import java.util.List;

import com.bytestorm.waygo.domain.valueobject.Pagina;

public record PaginaResponse<T>(
		List<T> conteudo,
		int pagina,
		int tamanho,
		long totalItens,
		int totalPaginas,
		boolean primeira,
		boolean ultima) {

	public static <T> PaginaResponse<T> de(Pagina<T> pagina) {
		return new PaginaResponse<>(
				pagina.itens(),
				pagina.numero(),
				pagina.tamanho(),
				pagina.totalItens(),
				pagina.totalPaginas(),
				pagina.primeira(),
				pagina.ultima());
	}

}
