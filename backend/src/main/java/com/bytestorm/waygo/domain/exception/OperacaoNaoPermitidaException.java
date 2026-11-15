package com.bytestorm.waygo.domain.exception;

/** O usuário está autenticado, mas a regra de negócio não permite a operação. Vira 403. */
public class OperacaoNaoPermitidaException extends RuntimeException {

	public OperacaoNaoPermitidaException(String mensagem) {
		super(mensagem);
	}

}
