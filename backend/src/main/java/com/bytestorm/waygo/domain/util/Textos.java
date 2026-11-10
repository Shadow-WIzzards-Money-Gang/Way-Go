package com.bytestorm.waygo.domain.util;

import java.text.Normalizer;
import java.util.Locale;

public final class Textos {

	private Textos() {
	}

	/** Remove acentos, converte para minúsculas e colapsa espaços, para comparações tolerantes. */
	public static String normalizar(String texto) {
		if (texto == null) {
			return "";
		}
		return Normalizer.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.trim()
				.replaceAll("\\s+", " ");
	}

}
