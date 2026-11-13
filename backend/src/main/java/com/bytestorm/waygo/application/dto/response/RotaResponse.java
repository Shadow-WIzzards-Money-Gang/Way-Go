package com.bytestorm.waygo.application.dto.response;

import java.util.List;

import com.bytestorm.waygo.domain.enums.CriterioRota;

public record RotaResponse(
		EstacaoResumoResponse origem,
		EstacaoResumoResponse destino,
		CriterioRota criterio,
		boolean alternativa,
		int tempoEstimadoSegundos,
		int tempoEstimadoMinutos,
		int distanciaMetros,
		int quantidadeBaldeacoes,
		List<String> avisos,
		List<TrechoRotaResponse> trechos,
		List<BaldeacaoRotaResponse> baldeacoes) {
}
