package com.bytestorm.waygo.application.dto.response;

import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.TipoTransporte;

public record LinhaResumoResponse(
		Long id,
		String nome,
		String codigo,
		String cor,
		String operadora,
		TipoTransporte tipoTransporte,
		boolean ativa) {

	public static LinhaResumoResponse de(LinhaTransporte linha) {
		return new LinhaResumoResponse(linha.getId(), linha.getNome(), linha.getCodigo(), linha.getCor(),
				linha.getOperadora(), linha.getTipoTransporte(), linha.isAtiva());
	}

}
