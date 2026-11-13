package com.bytestorm.waygo.domain.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conversão entre "HH:mm" e minutos desde a meia-noite (formato usado em ROTA_AGENDADA). */
public final class Horarios {

	private static final Pattern FORMATO = Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$");

	private Horarios() {
	}

	/** "07:10" -> 430. Lança IllegalArgumentException fora do formato HH:mm (00:00 a 23:59). */
	public static int paraMinutos(String horario) {
		Matcher matcher = horario == null ? null : FORMATO.matcher(horario.trim());
		if (matcher == null || !matcher.matches()) {
			throw new IllegalArgumentException("horário deve estar no formato HH:mm, entre 00:00 e 23:59");
		}
		return Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
	}

	/** 430 -> "07:10". */
	public static String formatar(int minutos) {
		return String.format("%02d:%02d", minutos / 60, minutos % 60);
	}

}
