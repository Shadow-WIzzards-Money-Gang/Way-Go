package com.bytestorm.waygo.domain.util;

import java.util.Objects;

public final class Validacoes {

	private Validacoes() {
	}

	public static <T> T exigir(T valor, String campo) {
		return Objects.requireNonNull(valor, campo + " é obrigatório");
	}

	/** Texto obrigatório, sem espaços nas pontas e com tamanho máximo (espelha o VARCHAR2 da coluna). */
	public static String exigirTexto(String valor, String campo, int tamanhoMaximo) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException(campo + " é obrigatório");
		}
		return limitarTamanho(valor.trim(), campo, tamanhoMaximo);
	}

	/** Texto opcional: null ou em branco vira null; caso contrário, aplica o tamanho máximo. */
	public static String textoOpcional(String valor, String campo, int tamanhoMaximo) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return limitarTamanho(valor.trim(), campo, tamanhoMaximo);
	}

	/** Valor opcional que, se informado, precisa ser maior que zero (espelha os CHECK "> 0" do banco). */
	public static Integer positivoOuNulo(Integer valor, String campo) {
		if (valor != null && valor <= 0) {
			throw new IllegalArgumentException(campo + " deve ser maior que zero");
		}
		return valor;
	}

	public static void exigirDiferentes(Object origem, Object destino) {
		if (origem.equals(destino)) {
			throw new IllegalArgumentException("origem e destino devem ser diferentes");
		}
	}

	private static String limitarTamanho(String valor, String campo, int tamanhoMaximo) {
		if (valor.length() > tamanhoMaximo) {
			throw new IllegalArgumentException(campo + " deve ter no máximo " + tamanhoMaximo + " caracteres");
		}
		return valor;
	}

}
