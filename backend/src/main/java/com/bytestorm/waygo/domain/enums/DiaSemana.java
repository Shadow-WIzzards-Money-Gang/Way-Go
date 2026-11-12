package com.bytestorm.waygo.domain.enums;

import java.time.DayOfWeek;

import com.bytestorm.waygo.domain.util.Textos;

public enum DiaSemana {

	DOMINGO("Domingo", DayOfWeek.SUNDAY),
	SEGUNDA("Segunda-feira", DayOfWeek.MONDAY),
	TERCA("Terça-feira", DayOfWeek.TUESDAY),
	QUARTA("Quarta-feira", DayOfWeek.WEDNESDAY),
	QUINTA("Quinta-feira", DayOfWeek.THURSDAY),
	SEXTA("Sexta-feira", DayOfWeek.FRIDAY),
	SABADO("Sábado", DayOfWeek.SATURDAY);

	private final String descricao;
	private final DayOfWeek dayOfWeek;

	DiaSemana(String descricao, DayOfWeek dayOfWeek) {
		this.descricao = descricao;
		this.dayOfWeek = dayOfWeek;
	}

	public String getDescricao() {
		return descricao;
	}

	public DayOfWeek toDayOfWeek() {
		return dayOfWeek;
	}

	public static DiaSemana de(DayOfWeek dayOfWeek) {
		for (DiaSemana dia : values()) {
			if (dia.dayOfWeek == dayOfWeek) {
				return dia;
			}
		}
		throw new IllegalArgumentException("Dia da semana inválido: " + dayOfWeek);
	}

	/** Aceita o nome da constante ("TERCA") ou a descrição ("Terça-feira"), ignorando maiúsculas e acentos. */
	public static DiaSemana de(String valor) {
		String normalizado = Textos.normalizar(valor);
		for (DiaSemana dia : values()) {
			if (Textos.normalizar(dia.descricao).equals(normalizado)
					|| Textos.normalizar(dia.name()).equals(normalizado)) {
				return dia;
			}
		}
		throw new IllegalArgumentException("Dia da semana inválido: " + valor);
	}

}
