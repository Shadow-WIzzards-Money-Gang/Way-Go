package com.bytestorm.waygo.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;

import lombok.Getter;

@Getter
public class LinhaStatus {

	private final Long id;
	private final Long linhaId;
	private final StatusOperacional status;
	private final String descricao;
	private final OrigemStatus origem;
	private final LocalDateTime atualizadoEm;

	public LinhaStatus(Long id, Long linhaId, StatusOperacional status, String descricao, OrigemStatus origem,
			LocalDateTime atualizadoEm) {
		this.id = id;
		this.linhaId = Objects.requireNonNull(linhaId, "linhaId é obrigatório");
		this.status = Objects.requireNonNull(status, "status é obrigatório");
		this.descricao = descricao;
		this.origem = Objects.requireNonNull(origem, "origem é obrigatória");
		this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "atualizadoEm é obrigatório");
	}

	public boolean mudouPara(StatusOperacional novoStatus) {
		return status != novoStatus;
	}

}
