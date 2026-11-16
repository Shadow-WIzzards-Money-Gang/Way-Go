package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.NumericBooleanConverter;
import org.hibernate.type.SqlTypes;

import com.bytestorm.waygo.domain.enums.Plataforma;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
@Table(name = "DISPOSITIVO_USUARIO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DispositivoUsuarioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "ID_USUARIO", length = 36, nullable = false)
	private UUID idUsuario;

	@Column(name = "TOKEN_FCM", nullable = false, length = 512, updatable = false)
	private String tokenFcm;

	@Enumerated(EnumType.STRING)
	@Column(name = "PLATAFORMA", nullable = false, length = 10)
	private Plataforma plataforma;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ALERTAS_STATUS", nullable = false)
	private boolean alertasStatus;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ATIVO", nullable = false)
	private boolean ativo;

	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "ATUALIZADO_EM", nullable = false)
	private LocalDateTime atualizadoEm;

	public DispositivoUsuarioEntity(Long id, UUID idUsuario, String tokenFcm, Plataforma plataforma,
			boolean alertasStatus, boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
		this.id = id;
		this.idUsuario = idUsuario;
		this.tokenFcm = tokenFcm;
		this.plataforma = plataforma;
		this.alertasStatus = alertasStatus;
		this.ativo = ativo;
		this.criadoEm = criadoEm;
		this.atualizadoEm = atualizadoEm;
	}

}
