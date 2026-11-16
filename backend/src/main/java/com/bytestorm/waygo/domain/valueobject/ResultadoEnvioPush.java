package com.bytestorm.waygo.domain.valueobject;

public record ResultadoEnvioPush(Tipo tipo, String mensagem) {

	public enum Tipo {
		SUCESSO,
		TOKEN_INVALIDO,
		FALHA_TEMPORARIA
	}

	public static ResultadoEnvioPush sucesso() {
		return new ResultadoEnvioPush(Tipo.SUCESSO, null);
	}

	public static ResultadoEnvioPush tokenInvalido(String mensagem) {
		return new ResultadoEnvioPush(Tipo.TOKEN_INVALIDO, mensagem);
	}

	public static ResultadoEnvioPush falhaTemporaria(String mensagem) {
		return new ResultadoEnvioPush(Tipo.FALHA_TEMPORARIA, mensagem);
	}

	public boolean deuCerto() {
		return tipo == Tipo.SUCESSO;
	}

}
