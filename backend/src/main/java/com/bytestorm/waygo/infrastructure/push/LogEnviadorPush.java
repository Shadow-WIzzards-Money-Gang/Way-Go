package com.bytestorm.waygo.infrastructure.push;

import java.util.Map;

import com.bytestorm.waygo.domain.interfaces.EnviadorPush;
import com.bytestorm.waygo.domain.valueobject.ResultadoEnvioPush;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogEnviadorPush implements EnviadorPush {

	@Override
	public ResultadoEnvioPush enviar(String tokenFcm, String titulo, String corpo, Map<String, String> dados) {
		log.info("Push (FCM desabilitado) para o aparelho ...{}: \"{}\" - {} {}", finalDo(tokenFcm), titulo, corpo,
				dados);
		return ResultadoEnvioPush.sucesso();
	}

	private static String finalDo(String token) {
		return token == null || token.length() <= 8 ? token : token.substring(token.length() - 8);
	}

}
