package com.bytestorm.waygo.application.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record RotaAgendadaRequest(
		@Schema(description = "Horário de partida (HH:mm, 24h)", example = "07:10")
		@NotBlank(message = "O horário de partida é obrigatório")
		String horario,

		@Schema(description = "Minutos de antecedência do lembrete (0 a 1440). Na criação, o padrão é 5; "
				+ "na atualização, se omitido, mantém o valor atual", example = "10")
		Integer antecedenciaMinutos,

		@Schema(description = "Dias da semana em que a rota é usada (nome da constante ou descrição)",
				example = "[\"SEGUNDA\", \"QUARTA\", \"SEXTA\"]")
		@NotEmpty(message = "Escolha ao menos um dia da semana")
		List<String> diasSemana,

		@Schema(description = "Agendamento ativo. Na criação, o padrão é true; na atualização, se omitido, "
				+ "mantém o valor atual")
		Boolean ativa) {
}
