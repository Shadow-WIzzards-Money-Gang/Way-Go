package com.bytestorm.waygo.domain.interfaces;

import java.util.Map;

import com.bytestorm.waygo.domain.valueobject.ResultadoEnvioPush;

public interface EnviadorPush {

	ResultadoEnvioPush enviar(String tokenFcm, String titulo, String corpo, Map<String, String> dados);

}
