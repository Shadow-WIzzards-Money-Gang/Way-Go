package com.bytestorm.waygo.application.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record EstacaoRequest(
		@Schema(description = "Nome da estação", example = "Higienópolis-Mackenzie")
		String nome,

		@Schema(description = "Latitude (-90 a 90)", example = "-23.5475")
		BigDecimal latitude,

		@Schema(description = "Longitude (-180 a 180)", example = "-46.6522")
		BigDecimal longitude,

		@Schema(description = "Só na atualização: false desativa a estação; true reativa")
		Boolean ativa) {
}
