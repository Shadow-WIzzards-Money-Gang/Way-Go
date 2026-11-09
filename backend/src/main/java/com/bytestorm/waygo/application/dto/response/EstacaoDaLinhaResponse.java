package com.bytestorm.waygo.application.dto.response;

import java.math.BigDecimal;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.valueobject.Coordenada;

public record EstacaoDaLinhaResponse(
		int sequencia,
		Long id,
		String nome,
		BigDecimal latitude,
		BigDecimal longitude,
		boolean ativa) {

	public static EstacaoDaLinhaResponse de(LinhaEstacao linhaEstacao, Estacao estacao) {
		Coordenada coordenada = estacao.getCoordenada();
		return new EstacaoDaLinhaResponse(
				linhaEstacao.getSequencia(),
				estacao.getId(),
				estacao.getNome(),
				coordenada == null ? null : coordenada.latitude(),
				coordenada == null ? null : coordenada.longitude(),
				linhaEstacao.isAtiva() && estacao.isAtiva());
	}

}
