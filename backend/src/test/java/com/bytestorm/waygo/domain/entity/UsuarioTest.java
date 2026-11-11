package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.interfaces.SenhaHasher;

class UsuarioTest {

	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 11, 10, 0);

	/** Imita BCrypt: o hash muda a cada chamada (salt), então só "corresponde" sabe conferir. */
	private static final SenhaHasher HASHER = new SenhaHasher() {
		@Override
		public String gerarHash(String senha) {
			return UUID.randomUUID() + ":" + senha;
		}

		@Override
		public boolean corresponde(String senha, String hash) {
			return hash.endsWith(":" + senha);
		}
	};

	private Usuario usuario() {
		return Usuario.criar("Joao@Email.com ", "senhaForte123", HASHER, AGORA);
	}

	@Test
	void criarGeraIdGuardaHashENormalizaEmail() {
		Usuario usuario = usuario();

		assertThat(usuario.getId()).isNotNull();
		assertThat(usuario.getEmail()).isEqualTo("joao@email.com");
		assertThat(usuario.getSenhaHash()).isNotEqualTo("senhaForte123");
		assertThat(usuario.getRoles()).containsExactly(Role.USUARIO);
		assertThat(usuario.getCriadoEm()).isEqualTo(AGORA);
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		UUID id = UUID.randomUUID();
		Usuario usuario = Usuario.reconstruir(id, "ana@x.com", "hash", Set.of(Role.ADMINISTRADOR, Role.USUARIO),
				AGORA);

		assertThat(usuario.getId()).isEqualTo(id);
		assertThat(usuario.getSenhaHash()).isEqualTo("hash");
		assertThat(usuario.getRoles()).containsExactlyInAnyOrder(Role.ADMINISTRADOR, Role.USUARIO);
		assertThat(usuario.isAdministrador()).isTrue();
	}

	@Test
	void verificaSenhaPeloHasherMesmoComSaltAleatorio() {
		Usuario usuario = usuario();

		assertThat(usuario.verificarSenha("senhaForte123", HASHER)).isTrue();
		assertThat(usuario.verificarSenha("errada123", HASHER)).isFalse();
		assertThat(usuario.verificarSenha(null, HASHER)).isFalse();
	}

	@Test
	void validaSenhaAntesDoHash() {
		assertThatThrownBy(() -> Usuario.criar("a@x.com", "curta", HASHER, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Usuario.criar("a@x.com", "x".repeat(73), HASHER, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Usuario.criar("a@x.com", "          ", HASHER, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void validaEComparaEmailSemDiferenciarMaiusculas() {
		assertThatThrownBy(() -> Usuario.criar("sem-arroba", "senhaForte123", HASHER, AGORA))
				.isInstanceOf(IllegalArgumentException.class);

		Usuario usuario = usuario();
		assertThat(usuario.possuiEmail("JOAO@email.COM")).isTrue();
		assertThat(usuario.possuiEmail("outro@email.com")).isFalse();
	}

	@Test
	void gerenciaRolesSemDeixarOUsuarioSemNenhuma() {
		Usuario usuario = usuario();

		usuario.adicionarRole(Role.MODERADOR);
		usuario.adicionarRole(Role.MODERADOR);
		assertThat(usuario.getRoles()).containsExactlyInAnyOrder(Role.USUARIO, Role.MODERADOR);

		usuario.removerRole(Role.MODERADOR);
		assertThatThrownBy(() -> usuario.removerRole(Role.USUARIO)).isInstanceOf(IllegalStateException.class);
		assertThat(usuario.possuiRole(Role.USUARIO)).isTrue();
	}

	@Test
	void rolesExpostasSaoSomenteLeitura() {
		Usuario usuario = usuario();

		assertThatThrownBy(() -> usuario.getRoles().add(Role.ADMINISTRADOR))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> Usuario.reconstruir(UUID.randomUUID(), "a@x.com", "hash", Set.of(), AGORA))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void roleDeAceitaNomeSemDiferenciarMaiusculas() {
		assertThat(Role.de(" administrador ")).isEqualTo(Role.ADMINISTRADOR);
		assertThatThrownBy(() -> Role.de("ADMIN")).isInstanceOf(IllegalArgumentException.class);
	}

}
