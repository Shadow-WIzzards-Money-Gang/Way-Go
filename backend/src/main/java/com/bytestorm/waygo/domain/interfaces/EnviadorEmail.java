package com.bytestorm.waygo.domain.interfaces;

public interface EnviadorEmail {

	/** Envia o e-mail de boas-vindas. Não lança exceção se o envio falhar. */
	void enviarBoasVindas(String destinatario);

}
