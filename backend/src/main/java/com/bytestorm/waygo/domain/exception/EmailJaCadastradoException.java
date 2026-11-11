package com.bytestorm.waygo.domain.exception;

public class EmailJaCadastradoException extends RuntimeException {

	public EmailJaCadastradoException() {
		super("E-mail já cadastrado");
	}

}
