package com.bytestorm.waygo.application.dto.request;

import com.bytestorm.waygo.domain.enums.StatusOperacional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record StatusLinhaRequest(
		@Schema(description = "Novo status operacional", example = "VELOCIDADE_REDUZIDA")
		@NotNull(message = "O status é obrigatório")
		StatusOperacional status,

		@Schema(description = "Explicação opcional mostrada aos passageiros (até 255 caracteres)",
				example = "Falha de sinalização entre Sé e Luz")
		String descricao) {
}
