package com.bytestorm.waygo.domain.entity;

import java.util.Objects;

import lombok.Getter;

@Getter
public class LinhaEstacao {

	private final Long id;
	private final Long linhaId;
	private final Long estacaoId;
	private final int sequencia;
	private final boolean ativa;

	public LinhaEstacao(Long id, Long linhaId, Long estacaoId, int sequencia, boolean ativa) {
		if (sequencia <= 0) {
			throw new IllegalArgumentException("sequencia deve ser maior que zero");
		}
		this.id = id;
		this.linhaId = Objects.requireNonNull(linhaId, "linhaId é obrigatório");
		this.estacaoId = Objects.requireNonNull(estacaoId, "estacaoId é obrigatório");
		this.sequencia = sequencia;
		this.ativa = ativa;
	}

}
