package com.bytestorm.waygo.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioResponse(
		UUID id,
		String email,
		@Schema(description = "Perfis da conta", example = "[\"USUARIO\"]")
		List<String> roles,
		@Schema(description = "Quando a conta foi criada")
		LocalDateTime criadoEm) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getEmail(),
				usuario.getRoles().stream().map(Enum::name).sorted().toList(),
				usuario.getCriadoEm());
	}

}
