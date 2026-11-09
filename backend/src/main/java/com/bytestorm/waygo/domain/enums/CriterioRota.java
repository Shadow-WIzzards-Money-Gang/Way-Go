package com.bytestorm.waygo.domain.enums;

public enum CriterioRota {

	MENOR_TEMPO,
	MENOS_BALDEACOES,
	EQUILIBRADO;

	// Grande o bastante para que uma baldeação a mais nunca compense qualquer ganho de tempo.
	private static final int PENALIDADE_MENOS_BALDEACOES_SEGUNDOS = 1_000_000;

	public int penalidadeBaldeacaoSegundos(int penalidadeConfigurada) {
		return switch (this) {
			case MENOR_TEMPO -> 0;
			case MENOS_BALDEACOES -> PENALIDADE_MENOS_BALDEACOES_SEGUNDOS;
			case EQUILIBRADO -> penalidadeConfigurada;
		};
	}

}
