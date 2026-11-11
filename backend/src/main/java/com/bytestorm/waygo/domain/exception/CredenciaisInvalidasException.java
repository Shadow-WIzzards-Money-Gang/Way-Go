package com.bytestorm.waygo.domain.exception;

/** E-mail ou senha incorretos. A mensagem é a mesma nos dois casos para não revelar quais e-mails existem. */
public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("E-mail ou senha inválidos");
	}

}
