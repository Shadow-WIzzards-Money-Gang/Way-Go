package com.bytestorm.waygo.infrastructure.config.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.bytestorm.waygo.domain.interfaces.GeradorCodigo;

@Component
public class GeradorCodigoSeguro implements GeradorCodigo {

	private static final int LIMITE = 1_000_000;

	private final SecureRandom random = new SecureRandom();

	@Override
	public String gerar() {
		return String.format("%06d", random.nextInt(LIMITE));
	}

}
