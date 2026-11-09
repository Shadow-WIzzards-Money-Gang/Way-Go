package com.bytestorm.waygo.domain.enums;

import java.util.Arrays;

public enum TipoTransporte {

	METRO("METRO"),
	TREM_METROPOLITANO("TREM METROPOLITANO"),
	MONOTRILHO("MONOTRILHO");

	private final String valor;

	TipoTransporte(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	public static TipoTransporte deValor(String valor) {
		return Arrays.stream(values())
				.filter(tipo -> tipo.valor.equals(valor))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Tipo de transporte desconhecido: " + valor));
	}

}
