package com.bytestorm.waygo.application.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record OrdemEstacoesRequest(
		@Schema(description = "Ids das estações na nova ordem (a primeira vira a sequência 1)",
				example = "[12, 7, 30]")
		@NotEmpty(message = "Informe as estações na nova ordem")
		List<Long> idsEstacoes) {
}
