package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TOKEN_RECUPERACAO_SENHA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRecuperacaoSenhaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "ID_USUARIO", length = 36, nullable = false, updatable = false)
	private UUID idUsuario;

	@Column(name = "CODIGO_HASH", nullable = false, length = 255, updatable = false)
	private String codigoHash;

	@Column(name = "EXPIRA_EM", nullable = false, updatable = false)
	private LocalDateTime expiraEm;

	@Column(name = "TENTATIVAS", nullable = false)
	private int tentativas;

	@Column(name = "USADO_EM")
	private LocalDateTime usadoEm;

	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	public TokenRecuperacaoSenhaEntity(Long id, UUID idUsuario, String codigoHash, LocalDateTime expiraEm,
			int tentativas, LocalDateTime usadoEm, LocalDateTime criadoEm) {
		this.id = id;
		this.idUsuario = idUsuario;
		this.codigoHash = codigoHash;
		this.expiraEm = expiraEm;
		this.tentativas = tentativas;
		this.usadoEm = usadoEm;
		this.criadoEm = criadoEm;
	}

}
