package com.bytestorm.waygo.application.dto.response;

import java.time.LocalDateTime;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.CriterioRota;

public record RotaSalvaResponse(
		Long id,
		String nome,
		EstacaoResumoResponse origem,
		EstacaoResumoResponse destino,
		CriterioRota criterio,
		LocalDateTime criadoEm) {

	public static RotaSalvaResponse de(RotaSalva rota, Estacao origem, Estacao destino) {
		return new RotaSalvaResponse(
				rota.getId(),
				rota.getNome(),
				EstacaoResumoResponse.de(origem),
				EstacaoResumoResponse.de(destino),
				rota.getCriterioRota(),
				rota.getCriadoEm());
	}

}
