package com.bytestorm.waygo.application.dto.response;

import java.util.List;

import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.TipoTransporte;

public record LinhaDetalheResponse(
		Long id,
		String nome,
		String codigo,
		String cor,
		String operadora,
		TipoTransporte tipoTransporte,
		boolean ativa,
		List<EstacaoDaLinhaResponse> estacoes) {

	public static LinhaDetalheResponse de(LinhaTransporte linha, List<EstacaoDaLinhaResponse> estacoes) {
		return new LinhaDetalheResponse(linha.getId(), linha.getNome(), linha.getCodigo(), linha.getCor(),
				linha.getOperadora(), linha.getTipoTransporte(), linha.isAtiva(), estacoes);
	}

}
