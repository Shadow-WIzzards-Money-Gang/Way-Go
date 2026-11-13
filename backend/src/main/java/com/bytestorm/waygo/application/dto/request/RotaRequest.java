package com.bytestorm.waygo.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RotaRequest(
		@Schema(description = "Nome da estação de origem (acentos e maiúsculas são ignorados)", example = "Sé")
		@NotBlank(message = "A estação de origem não pode ser nula ou vazia")
		String origem,

		@Schema(description = "Nome da estação de destino (acentos e maiúsculas são ignorados)", example = "Luz")
		@NotBlank(message = "A estação de destino não pode ser nula ou vazia")
		String destino,

		@Schema(description = "Critério de cálculo; se omitido, usa o padrão configurado",
				allowableValues = { "MENOR_TEMPO", "MENOS_BALDEACOES", "EQUILIBRADO" })
		String criterio,

		@Schema(description = "true calcula uma rota alternativa que usa só linhas com status "
				+ "\"Operação Normal\" (status desconhecido conta como normal)", defaultValue = "false")
		Boolean alternativa) {
}
