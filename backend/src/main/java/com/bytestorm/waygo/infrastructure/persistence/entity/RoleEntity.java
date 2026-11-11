package com.bytestorm.waygo.infrastructure.persistence.entity;

import com.bytestorm.waygo.domain.enums.Role;

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
@Table(name = "ROLE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "NOME", nullable = false, length = 30, unique = true)
	private Role nome;

}
