package com.bytestorm.waygo.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

/** Token de acesso emitido para um usuário, com o instante em que deixa de ser aceito. */
public record TokenAcesso(String token, Instant expiraEm) {

	public TokenAcesso {
		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("token é obrigatório");
		}
		Objects.requireNonNull(expiraEm, "expiraEm é obrigatório");
	}

}
