package com.bytestorm.waygo.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RotaSalvaRequest(
		@Schema(description = "Nome dado pelo usuário à rota", example = "Casa → Trabalho")
		@NotBlank(message = "O nome da rota é obrigatório")
		@Size(max = 100, message = "O nome da rota deve ter no máximo 100 caracteres")
		String nome,

		@Schema(description = "ID da estação de origem", example = "1")
		@NotNull(message = "A estação de origem é obrigatória")
		Long idEstacaoOrigem,

		@Schema(description = "ID da estação de destino", example = "2")
		@NotNull(message = "A estação de destino é obrigatória")
		Long idEstacaoDestino,

		@Schema(description = "Critério de cálculo; se omitido, usa o padrão configurado",
				allowableValues = { "MENOR_TEMPO", "MENOS_BALDEACOES", "EQUILIBRADO" })
		String criterio) {
}
