package com.bytestorm.waygo.domain.enums;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum StatusOperacional {

	NORMAL("Operação Normal"),
	VELOCIDADE_REDUZIDA("Velocidade Reduzida"),
	PARALISADA("Paralisada", "Operação Paralisada"),
	OPERACAO_ENCERRADA("Operação Encerrada"),
	OPERACAO_PARCIAL("Operação Parcial"),
	OPERACAO_TRANSITORIA("Operação Transitória"),
	ATIVIDADE_PROGRAMADA("Atividade Programada"),
	DESCONHECIDO;

	private final List<String> textos;

	StatusOperacional(String... textos) {
		this.textos = List.of(textos);
	}

	public List<String> getTextos() {
		return textos;
	}

	public static StatusOperacional deTexto(String texto) {
		if (texto == null) {
			return DESCONHECIDO;
		}
		String normalizado = normalizar(texto);
		return Arrays.stream(values())
				.filter(status -> status.textos.stream().map(StatusOperacional::normalizar).anyMatch(normalizado::equals))
				.findFirst()
				.orElse(DESCONHECIDO);
	}

	private static String normalizar(String texto) {
		return Normalizer.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.trim()
				.replaceAll("\\s+", " ");
	}

}
