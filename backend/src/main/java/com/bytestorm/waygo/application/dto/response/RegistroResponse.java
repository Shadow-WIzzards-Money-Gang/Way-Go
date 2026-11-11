package com.bytestorm.waygo.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.Usuario;

public record RegistroResponse(
		UUID id,
		String email,
		List<String> roles,
		LocalDateTime criadoEm) {

	public static RegistroResponse de(Usuario usuario) {
		return new RegistroResponse(
				usuario.getId(),
				usuario.getEmail(),
				usuario.getRoles().stream().map(Enum::name).sorted().toList(),
				usuario.getCriadoEm());
	}

}
