package com.bytestorm.waygo.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bytestorm.waygo.domain.entity.Usuario;

class BCryptSenhaHasherTest {

	private final BCryptSenhaHasher hasher = new BCryptSenhaHasher(new BCryptPasswordEncoder());

	@Test
	void gerarHashUsaBCryptComSaltAleatorio() {
		String hash = hasher.gerarHash("senhaForte123");

		assertThat(hash).startsWith("$2").isNotEqualTo("senhaForte123");
		assertThat(hasher.gerarHash("senhaForte123")).isNotEqualTo(hash);
	}

	@Test
	void correspondeSoComASenhaCerta() {
		String hash = hasher.gerarHash("senhaForte123");

		assertThat(hasher.corresponde("senhaForte123", hash)).isTrue();
		assertThat(hasher.corresponde("errada123", hash)).isFalse();
		assertThat(hasher.corresponde(null, hash)).isFalse();
		assertThat(hasher.corresponde("senhaForte123", null)).isFalse();
	}

	@Test
	void funcionaJuntoComOUsuarioDoDominio() {
		Usuario usuario = Usuario.criar("ana@email.com", "senhaForte123", hasher, LocalDateTime.now());

		assertThat(usuario.verificarSenha("senhaForte123", hasher)).isTrue();
		assertThat(usuario.verificarSenha("errada123", hasher)).isFalse();
	}

}
