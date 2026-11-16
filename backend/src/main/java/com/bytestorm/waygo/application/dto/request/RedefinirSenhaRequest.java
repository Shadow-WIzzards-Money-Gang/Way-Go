package com.bytestorm.waygo.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
		@Schema(description = "E-mail da conta")
		@Email(message = "O e-mail deve ser válido")
		@NotBlank(message = "O e-mail é obrigatório")
		String email,

		@Schema(description = "Código de 6 dígitos recebido por e-mail", example = "483920")
		@NotBlank(message = "O código é obrigatório")
		@Pattern(regexp = "\\d{6}", message = "O código deve ter 6 dígitos")
		String codigo,

		@Schema(description = "Nova senha (8 a 72 caracteres)")
		@NotBlank(message = "A nova senha é obrigatória")
		@Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
		String novaSenha) {
}
