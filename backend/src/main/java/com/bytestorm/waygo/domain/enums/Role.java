package com.bytestorm.waygo.domain.enums;

public enum Role {

	USUARIO,
	MODERADOR,
	ADMINISTRADOR;

	public static Role de(String valor) {
		for (Role role : values()) {
			if (role.name().equalsIgnoreCase(valor == null ? null : valor.trim())) {
				return role;
			}
		}
		throw new IllegalArgumentException("Role inválida: " + valor);
	}

}
