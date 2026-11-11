package com.bytestorm.waygo.infrastructure.config.security;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.interfaces.TokenProvider;
import com.bytestorm.waygo.domain.valueobject.TokenAcesso;

@Service
public class TokenService implements TokenProvider {

	private static final String CLAIM_ID = "id";
	private static final String CLAIM_ROLES = "roles";

	@Value("${seguranca.jwt.segredo}")
	private String SEGREDO;

	@Value("${seguranca.jwt.emissor}")
	private String EMISSOR;

	@Value("${seguranca.jwt.expiracao.minutos}")
	private Long EXPIRACAO_MINUTOS;

	@Override
	public TokenAcesso gerarToken(Usuario usuario) {
		Instant emitidoEm = emitidoEm();
		Instant expiraEm = expiradoEm(emitidoEm);
		String token = JWT.create()
				.withIssuer(EMISSOR)
				.withSubject(usuario.getEmail())
				.withClaim(CLAIM_ID, usuario.getId().toString())
				.withClaim(CLAIM_ROLES, usuario.getRoles().stream().map(Enum::name).sorted().toList())
				.withIssuedAt(emitidoEm)
				.withExpiresAt(expiraEm)
				.sign(getAlgoritmo());
		return new TokenAcesso(token, expiraEm);
	}

	@Override
	public Optional<String> validarToken(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(
				JWT.require(getAlgoritmo())
					.withIssuer(EMISSOR)
					.build()
				.verify(token)
				.getSubject()
			);
		} catch (JWTVerificationException e) {
			return Optional.empty();
		}
	}

	private Instant emitidoEm() {
		return Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS);
	}

	private Instant expiradoEm(Instant emitidoEm) {
		return emitidoEm.plus(EXPIRACAO_MINUTOS, ChronoUnit.MINUTES);
	}

	private Algorithm getAlgoritmo() {
		return Algorithm.HMAC256(SEGREDO);
	}

}
