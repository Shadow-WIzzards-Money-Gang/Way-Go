package com.bytestorm.waygo.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequest(
		@Schema(description = "Senha atual do usuário")
		@NotBlank(message = "A senha atual é obrigatória")
		String senhaAtual,

		@Schema(description = "Nova senha (8 a 72 caracteres)")
		@NotBlank(message = "A nova senha é obrigatória")
		String novaSenha) {
}
