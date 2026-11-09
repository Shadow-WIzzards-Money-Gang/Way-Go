package com.bytestorm.waygo.domain.entity;

import java.util.Objects;

import lombok.Getter;

@Getter
public class Baldeacao {

	private final Long id;
	private final Long linhaEstacaoOrigemId;
	private final Long linhaEstacaoDestinoId;
	private final Integer tempoBaldeacaoSegundos;
	private final boolean ativa;

	public Baldeacao(Long id, Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId, Integer tempoBaldeacaoSegundos,
			boolean ativa) {
		Objects.requireNonNull(linhaEstacaoOrigemId, "linhaEstacaoOrigemId é obrigatório");
		Objects.requireNonNull(linhaEstacaoDestinoId, "linhaEstacaoDestinoId é obrigatório");
		if (linhaEstacaoOrigemId.equals(linhaEstacaoDestinoId)) {
			throw new IllegalArgumentException("origem e destino devem ser diferentes");
		}
		this.id = id;
		this.linhaEstacaoOrigemId = linhaEstacaoOrigemId;
		this.linhaEstacaoDestinoId = linhaEstacaoDestinoId;
		this.tempoBaldeacaoSegundos = tempoBaldeacaoSegundos;
		this.ativa = ativa;
	}

}
