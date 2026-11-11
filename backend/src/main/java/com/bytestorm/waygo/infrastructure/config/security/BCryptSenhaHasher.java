package com.bytestorm.waygo.infrastructure.config.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bytestorm.waygo.domain.interfaces.SenhaHasher;

@Component
public class BCryptSenhaHasher implements SenhaHasher {

	private final PasswordEncoder passwordEncoder;

	public BCryptSenhaHasher(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String gerarHash(String senhaEmTexto) {
		return passwordEncoder.encode(senhaEmTexto);
	}

	@Override
	public boolean corresponde(String senhaEmTexto, String hash) {
		return senhaEmTexto != null && hash != null && passwordEncoder.matches(senhaEmTexto, hash);
	}

}
