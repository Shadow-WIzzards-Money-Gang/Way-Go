package com.bytestorm.waygo.domain.entity;

import com.bytestorm.waygo.domain.valueobject.Coordenada;

import lombok.Getter;

@Getter
public class Estacao {

	private final Long id;
	private final String nome;
	private final Coordenada coordenada;
	private final boolean ativa;

	public Estacao(Long id, String nome, Coordenada coordenada, boolean ativa) {
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome é obrigatório");
		}
		this.id = id;
		this.nome = nome;
		this.coordenada = coordenada;
		this.ativa = ativa;
	}

}
