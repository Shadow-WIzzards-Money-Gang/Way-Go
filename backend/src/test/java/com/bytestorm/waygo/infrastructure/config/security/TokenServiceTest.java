package com.bytestorm.waygo.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.interfaces.SenhaHasher;

class TokenServiceTest {

	private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres";
	private static final String EMISSOR = "way-go";

	/** O TokenService não usa o hasher; basta um que funcione para criar o usuário. */
	private static final SenhaHasher HASHER = new SenhaHasher() {
		@Override
		public String gerarHash(String senha) {
			return "hash:" + senha;
		}

		@Override
		public boolean corresponde(String senha, String hash) {
			return hash.equals("hash:" + senha);
		}
	};

	private final Usuario usuario = Usuario.criar("Ana@Email.com", "senhaForte123", HASHER, LocalDateTime.now());

	/** Preenche os campos que o Spring injeta via @Value. */
	private TokenService tokenService(String segredo, String emissor, long expiracaoMinutos) {
		TokenService tokenService = new TokenService();
		ReflectionTestUtils.setField(tokenService, "SEGREDO", segredo);
		ReflectionTestUtils.setField(tokenService, "EMISSOR", emissor);
		ReflectionTestUtils.setField(tokenService, "EXPIRACAO_MINUTOS", expiracaoMinutos);
		return tokenService;
	}

	private TokenService tokenService() {
		return tokenService(SEGREDO, EMISSOR, 60L);
	}

	@Test
	void tokenGeradoEhValidoEDevolveOEmailNormalizado() {
		TokenService tokenService = tokenService();

		String token = tokenService.gerarToken(usuario);

		assertThat(tokenService.validarToken(token)).contains("ana@email.com");
	}

	@Test
	void tokenCarregaIdRolesEmissorEExpiracao() {
		Instant antes = Instant.now().truncatedTo(ChronoUnit.SECONDS);

		DecodedJWT jwt = JWT.decode(tokenService().gerarToken(usuario));

		assertThat(jwt.getIssuer()).isEqualTo(EMISSOR);
		assertThat(jwt.getSubject()).isEqualTo("ana@email.com");
		assertThat(UUID.fromString(jwt.getClaim("id").asString())).isEqualTo(usuario.getId());
		assertThat(jwt.getClaim("roles").asList(String.class)).containsExactly(Role.USUARIO.name());
		assertThat(jwt.getIssuedAtAsInstant()).isAfterOrEqualTo(antes);
		assertThat(jwt.getExpiresAtAsInstant()).isEqualTo(jwt.getIssuedAtAsInstant().plus(60, ChronoUnit.MINUTES));
	}

	@Test
	void rejeitaTokenVazioMalformadoOuAdulterado() {
		TokenService tokenService = tokenService();
		String token = tokenService.gerarToken(usuario);

		assertThat(tokenService.validarToken(null)).isEmpty();
		assertThat(tokenService.validarToken("  ")).isEmpty();
		assertThat(tokenService.validarToken("nao-eh-jwt")).isEmpty();
		assertThat(tokenService.validarToken(token + "x")).isEmpty();
	}

	@Test
	void rejeitaTokenAssinadoComOutroSegredoOuDeOutroEmissor() {
		String token = tokenService().gerarToken(usuario);

		assertThat(tokenService(SEGREDO + "-outro", EMISSOR, 60L).validarToken(token)).isEmpty();
		assertThat(tokenService(SEGREDO, "outro-emissor", 60L).validarToken(token)).isEmpty();
	}

	@Test
	void rejeitaTokenExpirado() {
		String expirado = tokenService(SEGREDO, EMISSOR, -1L).gerarToken(usuario);

		assertThat(tokenService().validarToken(expirado)).isEmpty();
	}

}
