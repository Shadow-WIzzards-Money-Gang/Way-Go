package com.bytestorm.waygo.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(
		@Schema(description = "E-mail da conta que terá a senha redefinida")
		@Email(message = "O e-mail deve ser válido")
		@NotBlank(message = "O e-mail é obrigatório")
		String email) {
}
