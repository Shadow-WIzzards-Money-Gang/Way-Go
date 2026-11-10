package com.bytestorm.waygo.domain.enums;

import com.bytestorm.waygo.domain.util.Textos;

public enum CriterioRota {

	MENOR_TEMPO("Menor tempo"),
	MENOS_BALDEACOES("Menos baldeações"),
	EQUILIBRADO("Equilibrado");

	// Grande o bastante para que uma baldeação a mais nunca compense qualquer ganho de tempo.
	private static final int PENALIDADE_MENOS_BALDEACOES_SEGUNDOS = 1_000_000;

	private final String descricao;

	CriterioRota(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	public int penalidadeBaldeacaoSegundos(int penalidadeConfigurada) {
		return switch (this) {
			case MENOR_TEMPO -> 0;
			case MENOS_BALDEACOES -> PENALIDADE_MENOS_BALDEACOES_SEGUNDOS;
			case EQUILIBRADO -> penalidadeConfigurada;
		};
	}

	/**
	 * Aceita o nome da constante ("MENOS_BALDEACOES") ou a descrição ("Menos baldeações"), ignorando maiúsculas,
	 * acentos e a troca de "_" por espaço.
	 */
	public static CriterioRota de(String valor) {
		String normalizado = Textos.normalizar(valor).replace('_', ' ');
		for (CriterioRota criterio : values()) {
			if (Textos.normalizar(criterio.descricao).equals(normalizado)
					|| Textos.normalizar(criterio.name()).replace('_', ' ').equals(normalizado)) {
				return criterio;
			}
		}
		throw new IllegalArgumentException("Critério de rota inválido: " + valor);
	}

}
