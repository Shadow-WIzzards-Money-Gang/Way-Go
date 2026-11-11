package com.bytestorm.waygo.application.dto.response;

import java.time.Duration;
import java.time.Instant;

import com.bytestorm.waygo.domain.valueobject.TokenAcesso;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
		@Schema(description = "Token JWT, enviado no header Authorization: Bearer <token>")
		String token,

		@Schema(description = "Tipo do token", example = "Bearer")
		String tipo,

		@Schema(description = "Instante de expiração do token (ISO-8601, UTC)", example = "2026-11-11T15:00:00Z")
		Instant expiraEm,

		@Schema(description = "Segundos até o token expirar, contados a partir da resposta", example = "3600")
		long expiraEmSegundos) {

	private static final String TIPO_BEARER = "Bearer";

	public static LoginResponse de(TokenAcesso tokenAcesso, Instant agora) {
		long segundosRestantes = Math.max(0, Duration.between(agora, tokenAcesso.expiraEm()).toSeconds());
		return new LoginResponse(tokenAcesso.token(), TIPO_BEARER, tokenAcesso.expiraEm(), segundosRestantes);
	}

}
