package com.bytestorm.waygo.domain.valueobject;

import java.util.Map;
import java.util.Objects;

import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoConexao;

public record ParametrosRota(
		CriterioRota criterio,
		double fatorLentidao,
		int penalidadeBaldeacaoSegundos,
		Map<Long, StatusOperacional> statusPorLinha,
		boolean somenteLinhasNormais) {

	public ParametrosRota(CriterioRota criterio, double fatorLentidao, int penalidadeBaldeacaoSegundos,
			Map<Long, StatusOperacional> statusPorLinha) {
		this(criterio, fatorLentidao, penalidadeBaldeacaoSegundos, statusPorLinha, false);
	}

	public ParametrosRota {
		Objects.requireNonNull(criterio, "criterio é obrigatório");
		if (fatorLentidao < 1) {
			throw new IllegalArgumentException("fatorLentidao deve ser maior ou igual a 1");
		}
		if (penalidadeBaldeacaoSegundos < 0) {
			throw new IllegalArgumentException("penalidadeBaldeacaoSegundos não pode ser negativa");
		}
		statusPorLinha = Map.copyOf(statusPorLinha);
	}

	public StatusOperacional statusDa(Long linhaId) {
		return statusPorLinha.getOrDefault(linhaId, StatusOperacional.NORMAL);
	}

	public boolean linhaDisponivel(Long linhaId) {
		StatusOperacional status = statusDa(linhaId);
		return somenteLinhasNormais ? status.operacaoNormal() : !status.bloqueiaCirculacao();
	}

	/** Tempo que o passageiro deve levar na aresta, já ajustado pelo status da linha. */
	public int tempoEstimadoSegundos(ArestaRede aresta, NoRede origem) {
		if (aresta.tipo() == TipoConexao.CONEXAO && statusDa(origem.linhaId()).afetaTempo()) {
			return (int) Math.round(aresta.tempoSegundos() * fatorLentidao);
		}
		return aresta.tempoSegundos();
	}

	/** Peso usado pelo algoritmo: o tempo estimado mais a penalidade do critério nas baldeações. */
	public long custo(ArestaRede aresta, NoRede origem) {
		long tempo = tempoEstimadoSegundos(aresta, origem);
		if (aresta.tipo() == TipoConexao.BALDEACAO) {
			tempo += criterio.penalidadeBaldeacaoSegundos(penalidadeBaldeacaoSegundos);
		}
		return tempo;
	}

}
