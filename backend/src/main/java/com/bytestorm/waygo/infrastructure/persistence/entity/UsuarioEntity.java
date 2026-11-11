package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USUARIO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioEntity {

	@Id
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "ID", length = 36, nullable = false, updatable = false)
	private UUID id;

	@Column(name = "EMAIL", nullable = false, length = 255)
	private String email;

	@Column(name = "SENHA_HASH", nullable = false, length = 255)
	private String senhaHash;

	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "USUARIO_ROLE",
			joinColumns = @JoinColumn(name = "ID_USUARIO"),
			inverseJoinColumns = @JoinColumn(name = "ID_ROLE"))
	private Set<RoleEntity> roles = new HashSet<>();

	public UsuarioEntity(UUID id, String email, String senhaHash, LocalDateTime criadoEm, Set<RoleEntity> roles) {
		this.id = id;
		this.email = email;
		this.senhaHash = senhaHash;
		this.criadoEm = criadoEm;
		this.roles = new HashSet<>(roles);
	}

}
