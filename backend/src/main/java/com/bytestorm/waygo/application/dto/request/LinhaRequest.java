package com.bytestorm.waygo.application.dto.request;

import com.bytestorm.waygo.domain.enums.TipoTransporte;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinhaRequest(
		@Schema(description = "Nome da linha", example = "Linha 6-Laranja")
		@NotBlank(message = "O nome é obrigatório")
		String nome,

		@Schema(description = "Código único da linha", example = "6")
		@NotBlank(message = "O código é obrigatório")
		String codigo,

		@Schema(description = "Cor no formato #RRGGBB (opcional)", example = "#F28C00")
		String cor,

		@Schema(description = "Empresa que opera a linha", example = "Linha Uni")
		@NotBlank(message = "A operadora é obrigatória")
		String operadora,

		@Schema(description = "Tipo de transporte", example = "METRO")
		@NotNull(message = "O tipo de transporte é obrigatório")
		TipoTransporte tipoTransporte) {
}
