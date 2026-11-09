package com.bytestorm.waygo.domain.entity;

import java.util.Objects;

import com.bytestorm.waygo.domain.enums.TipoTransporte;

import lombok.Getter;

@Getter
public class LinhaTransporte {

	private final Long id;
	private final String nome;
	private final String codigo;
	private final String cor;
	private final String operadora;
	private final TipoTransporte tipoTransporte;
	private final boolean ativa;

	public LinhaTransporte(Long id, String nome, String codigo, String cor, String operadora,
			TipoTransporte tipoTransporte, boolean ativa) {
		this.id = id;
		this.nome = exigirTexto(nome, "nome");
		this.codigo = exigirTexto(codigo, "codigo");
		this.cor = cor;
		this.operadora = exigirTexto(operadora, "operadora");
		this.tipoTransporte = Objects.requireNonNull(tipoTransporte, "tipoTransporte é obrigatório");
		this.ativa = ativa;
	}

	private static String exigirTexto(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException(campo + " é obrigatório");
		}
		return valor;
	}

}
