package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bytestorm.waygo.domain.enums.CriterioRota;

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
@Table(name = "ROTA_SALVA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RotaSalvaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "NOME", nullable = false, length = 100)
	private String nome;

	@Column(name = "ID_ESTACAO_ORIGEM", nullable = false)
	private Long idEstacaoOrigem;

	@Column(name = "ID_ESTACAO_DESTINO", nullable = false)
	private Long idEstacaoDestino;

	@Enumerated(EnumType.STRING)
	@Column(name = "CRITERIO", nullable = false, length = 30)
	private CriterioRota criterio;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "ID_USUARIO", length = 36, nullable = false, updatable = false)
	private UUID idUsuario;

	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	public RotaSalvaEntity(Long id, String nome, Long idEstacaoOrigem, Long idEstacaoDestino, CriterioRota criterio,
			UUID idUsuario, LocalDateTime criadoEm) {
		this.id = id;
		this.nome = nome;
		this.idEstacaoOrigem = idEstacaoOrigem;
		this.idEstacaoDestino = idEstacaoDestino;
		this.criterio = criterio;
		this.idUsuario = idUsuario;
		this.criadoEm = criadoEm;
	}

}
