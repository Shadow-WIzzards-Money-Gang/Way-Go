package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LINHA_STATUS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinhaStatusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ID_LINHA", nullable = false)
	private Long linhaId;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 50)
	private StatusOperacional status;

	@Column(name = "DESCRICAO", length = 255)
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGEM", nullable = false, length = 30)
	private OrigemStatus origem;

	@Column(name = "ATUALIZADO_EM", nullable = false)
	private LocalDateTime atualizadoEm;

	public LinhaStatusEntity(Long linhaId, StatusOperacional status, String descricao, OrigemStatus origem,
			LocalDateTime atualizadoEm) {
		this.linhaId = linhaId;
		this.status = status;
		this.descricao = descricao;
		this.origem = origem;
		this.atualizadoEm = atualizadoEm;
	}

}
