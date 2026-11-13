package com.bytestorm.waygo.domain.exception;

/** O recurso conflita com outro já existente (ex.: código de linha repetido). Vira 409. */
public class ConflitoException extends RuntimeException {

	public ConflitoException(String mensagem) {
		super(mensagem);
	}

}
