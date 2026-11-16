package com.bytestorm.waygo.domain.interfaces;

public interface EnviadorEmail {

	void enviarBoasVindas(String destinatario);

	void enviarContaExcluida(String destinatario);

	void enviarCodigoRecuperacao(String destinatario, String codigo);

	void enviarSenhaRedefinida(String destinatario);

}
