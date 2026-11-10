package com.bytestorm.waygo.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoTransporte;

/**
 * Grafo da rede em operação, no formato pensado para desenhar um mapa: estações são os pontos, linhas trazem cor,
 * status e a sequência de estações (polilinha), conexões e baldeações são os segmentos entre estações.
 */
public record RedeResponse(
		List<EstacaoRede> estacoes,
		List<LinhaRede> linhas,
		List<ConexaoRede> conexoes,
		List<BaldeacaoRede> baldeacoes) {

	public record EstacaoRede(
			Long id,
			String nome,
			BigDecimal latitude,
			BigDecimal longitude,
			List<Long> linhaIds) {
	}

	public record LinhaRede(
			Long id,
			String nome,
			String codigo,
			String cor,
			String operadora,
			TipoTransporte tipoTransporte,
			StatusOperacional status,
			String statusDescricao,
			boolean disponivel,
			List<Long> estacaoIds) {
	}

	/** Trecho entre duas estações vizinhas de uma mesma linha (não direcionado). */
	public record ConexaoRede(
			Long linhaId,
			Long estacaoOrigemId,
			Long estacaoDestinoId,
			int tempoSegundos,
			int distanciaMetros) {
	}

	/** Troca de linha, que pode ser na mesma estação ou entre estações ligadas por passagem (não direcionada). */
	public record BaldeacaoRede(
			Long estacaoOrigemId,
			Long linhaOrigemId,
			Long estacaoDestinoId,
			Long linhaDestinoId,
			int tempoSegundos) {
	}

}
