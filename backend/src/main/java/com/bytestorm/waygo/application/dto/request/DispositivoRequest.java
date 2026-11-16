package com.bytestorm.waygo.application.dto.request;

import com.bytestorm.waygo.domain.enums.Plataforma;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DispositivoRequest(
		@Schema(description = "Token FCM do aparelho")
		@NotBlank(message = "O token é obrigatório")
		@Size(max = 512, message = "O token deve ter no máximo 512 caracteres")
		String token,

		@Schema(description = "Sistema do aparelho", allowableValues = { "ANDROID", "IOS" })
		@NotNull(message = "A plataforma é obrigatória")
		Plataforma plataforma,

		@Schema(description = "Receber alertas de mudança de status das linhas das rotas salvas",
				defaultValue = "true")
		Boolean alertasStatus) {
}
