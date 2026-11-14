package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.interfaces.SenhaHasher;

import lombok.Getter;

@Getter
public class Usuario {

	private static final int TAMANHO_EMAIL = 255;
	private static final int TAMANHO_HASH = 255;
	private static final int SENHA_MINIMO = 8;
	private static final int SENHA_MAXIMO = 72;
	private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final UUID id;
	private final LocalDateTime criadoEm;
	private String email;
	private String senhaHash;
	private final Set<Role> roles;

	private Usuario(UUID id, String email, String senhaHash, Set<Role> roles, LocalDateTime criadoEm) {
		this.id = exigir(id, "id");
		this.email = validarEmail(email);
		this.senhaHash = validarHash(senhaHash);
		this.roles = validarRoles(roles);
		this.criadoEm = exigir(criadoEm, "criadoEm");
	}

	public static Usuario criar(String email, String senha, SenhaHasher hasher, LocalDateTime criadoEm) {
		return reconstruir(UUID.randomUUID(), email, gerarHash(senha, hasher), Set.of(Role.USUARIO), criadoEm);
	}

	public static Usuario reconstruir(UUID id, String email, String senhaHash, Set<Role> roles,
			LocalDateTime criadoEm) {
		return new Usuario(id, email, senhaHash, roles, criadoEm);
	}

	public boolean verificarSenha(String senha, SenhaHasher hasher) {
		return senha != null && exigir(hasher, "hasher").corresponde(senha, senhaHash);
	}

	public boolean possuiEmail(String outroEmail) {
		return outroEmail != null && email.equals(normalizarEmail(outroEmail));
	}

	public void atualizarSenha(String senhaAtual, String novaSenha, SenhaHasher hasher) {
		if (!verificarSenha(senhaAtual, hasher)) {
			throw new IllegalArgumentException("senha atual incorreta");
		}
		this.senhaHash = validarHash(gerarHash(novaSenha, hasher));
	}

	public void adicionarRole(Role role) {
		roles.add(exigir(role, "role"));
	}

	public void removerRole(Role role) {
		if (roles.contains(role) && roles.size() == 1) {
			throw new IllegalStateException("o usuário precisa ter ao menos uma role");
		}
		roles.remove(role);
	}

	public boolean possuiRole(Role role) {
		return roles.contains(role);
	}

	public boolean isAdministrador() {
		return possuiRole(Role.ADMINISTRADOR);
	}

	public Set<Role> getRoles() {
		return Set.copyOf(roles);
	}

	private static String gerarHash(String senha, SenhaHasher hasher) {
		validarSenha(senha);
		return validarHash(exigir(hasher, "hasher").gerarHash(senha));
	}

	private static void validarSenha(String senha) {
		if (senha == null || senha.length() < SENHA_MINIMO) {
			throw new IllegalArgumentException("senha deve ter ao menos " + SENHA_MINIMO + " caracteres");
		}
		if (senha.length() > SENHA_MAXIMO) {
			throw new IllegalArgumentException("senha deve ter no máximo " + SENHA_MAXIMO + " caracteres");
		}
		if (senha.isBlank()) {
			throw new IllegalArgumentException("senha não pode ser só espaços");
		}
	}

	private static String validarEmail(String email) {
		String normalizado = normalizarEmail(exigirTexto(email, "email", TAMANHO_EMAIL));
		if (!FORMATO_EMAIL.matcher(normalizado).matches()) {
			throw new IllegalArgumentException("email inválido");
		}
		return normalizado;
	}

	private static String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static String validarHash(String hash) {
		return exigirTexto(hash, "senhaHash", TAMANHO_HASH);
	}

	private static Set<Role> validarRoles(Set<Role> roles) {
		if (roles == null || roles.isEmpty()) {
			throw new IllegalArgumentException("o usuário precisa ter ao menos uma role");
		}
		return EnumSet.copyOf(roles);
	}

}
