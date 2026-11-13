package com.bytestorm.waygo.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record LinhaAtualizacaoRequest(
		@Schema(description = "Novo nome", example = "Linha 6-Laranja")
		String nome,

		@Schema(description = "Nova cor no formato #RRGGBB", example = "#F28C00")
		String cor,

		@Schema(description = "Nova operadora", example = "Linha Uni")
		String operadora,

		@Schema(description = "false desativa a linha (some do cálculo de rotas); true reativa")
		Boolean ativa) {
}
