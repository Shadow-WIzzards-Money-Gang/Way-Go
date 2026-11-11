package com.bytestorm.waygo.domain.interfaces;

public interface SenhaHasher {

	String gerarHash(String senhaEmTexto);

	boolean corresponde(String senhaEmTexto, String hash);

}
