package com.bytestorm.waygo.application.dto.response;

import com.bytestorm.waygo.domain.entity.LinhaTransporte;

public record LinhaDaEstacaoResponse(
		Long id,
		String nome,
		String codigo,
		String cor) {

	public static LinhaDaEstacaoResponse de(LinhaTransporte linha) {
		return new LinhaDaEstacaoResponse(linha.getId(), linha.getNome(), linha.getCodigo(), linha.getCor());
	}

}
