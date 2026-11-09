package com.bytestorm.waygo.infrastructure.client.trilhosagora.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LinhaTrilhosAgora(
		int linha,
		String nome,
		String rotulo,
		String status,
		String descricao,
		String operadora,
		@JsonProperty("atualizado_em") OffsetDateTime atualizadoEm) {

}
