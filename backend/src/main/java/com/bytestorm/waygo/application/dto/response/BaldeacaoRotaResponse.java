package com.bytestorm.waygo.application.dto.response;

public record BaldeacaoRotaResponse(
		EstacaoResumoResponse estacaoOrigem,
		EstacaoResumoResponse estacaoDestino,
		LinhaResumoResponse linhaOrigem,
		LinhaResumoResponse linhaDestino,
		int tempoSegundos) {
}
