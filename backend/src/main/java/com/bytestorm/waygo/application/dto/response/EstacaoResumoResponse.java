package com.bytestorm.waygo.application.dto.response;

import java.math.BigDecimal;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.valueobject.Coordenada;

public record EstacaoResumoResponse(
		Long id,
		String nome,
		BigDecimal latitude,
		BigDecimal longitude,
		boolean ativa) {

	public static EstacaoResumoResponse de(Estacao estacao) {
		Coordenada coordenada = estacao.getCoordenada();
		return new EstacaoResumoResponse(
				estacao.getId(),
				estacao.getNome(),
				coordenada == null ? null : coordenada.latitude(),
				coordenada == null ? null : coordenada.longitude(),
				estacao.isAtiva());
	}

}
