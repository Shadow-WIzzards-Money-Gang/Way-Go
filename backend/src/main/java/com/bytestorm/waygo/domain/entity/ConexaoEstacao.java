package com.bytestorm.waygo.domain.entity;

import java.util.Objects;

import lombok.Getter;

@Getter
public class ConexaoEstacao {

	private final Long id;
	private final Long linhaEstacaoOrigemId;
	private final Long linhaEstacaoDestinoId;
	private final Integer tempoViagemSegundos;
	private final Integer distanciaMetros;
	private final boolean ativa;

	public ConexaoEstacao(Long id, Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId, Integer tempoViagemSegundos,
			Integer distanciaMetros, boolean ativa) {
		Objects.requireNonNull(linhaEstacaoOrigemId, "linhaEstacaoOrigemId é obrigatório");
		Objects.requireNonNull(linhaEstacaoDestinoId, "linhaEstacaoDestinoId é obrigatório");
		if (linhaEstacaoOrigemId.equals(linhaEstacaoDestinoId)) {
			throw new IllegalArgumentException("origem e destino devem ser diferentes");
		}
		this.id = id;
		this.linhaEstacaoOrigemId = linhaEstacaoOrigemId;
		this.linhaEstacaoDestinoId = linhaEstacaoDestinoId;
		this.tempoViagemSegundos = tempoViagemSegundos;
		this.distanciaMetros = distanciaMetros;
		this.ativa = ativa;
	}

}
