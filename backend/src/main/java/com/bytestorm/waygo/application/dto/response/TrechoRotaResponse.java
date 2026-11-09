package com.bytestorm.waygo.application.dto.response;

import java.util.List;

import com.bytestorm.waygo.domain.enums.StatusOperacional;

public record TrechoRotaResponse(
		int ordem,
		LinhaResumoResponse linha,
		StatusOperacional statusLinha,
		EstacaoResumoResponse embarque,
		EstacaoResumoResponse desembarque,
		EstacaoResumoResponse sentido,
		List<EstacaoResumoResponse> estacoes,
		int quantidadeParadas,
		int tempoSegundos,
		int distanciaMetros) {
}
