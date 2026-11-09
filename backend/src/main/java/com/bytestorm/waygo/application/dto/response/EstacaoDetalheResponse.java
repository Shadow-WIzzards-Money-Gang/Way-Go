package com.bytestorm.waygo.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.valueobject.Coordenada;

public record EstacaoDetalheResponse(
		Long id,
		String nome,
		BigDecimal latitude,
		BigDecimal longitude,
		boolean ativa,
		List<LinhaDaEstacaoResponse> linhas) {

	public static EstacaoDetalheResponse de(Estacao estacao, List<LinhaDaEstacaoResponse> linhas) {
		Coordenada coordenada = estacao.getCoordenada();
		return new EstacaoDetalheResponse(
				estacao.getId(),
				estacao.getNome(),
				coordenada == null ? null : coordenada.latitude(),
				coordenada == null ? null : coordenada.longitude(),
				estacao.isAtiva(),
				linhas);
	}

}
