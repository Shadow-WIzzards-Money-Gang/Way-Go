package com.bytestorm.waygo.domain.interfaces;

import java.util.Optional;

import com.bytestorm.waygo.domain.entity.Usuario;

/**
 * Porta para emissão e validação de tokens de acesso. A aplicação depende só desta interface;
 * a implementação (JWT) fica na infraestrutura.
 */
public interface TokenProvider {

	/** Gera um token de acesso assinado para o usuário. */
	String gerarToken(Usuario usuario);

	/**
	 * Valida assinatura, emissor e expiração do token.
	 *
	 * @return o e-mail do usuário dono do token, ou vazio se o token for inválido ou estiver expirado
	 */
	Optional<String> validarToken(String token);

}
