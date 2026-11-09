package com.bytestorm.waygo.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trilhos-agora")
public record TrilhosAgoraProperties(
		String url,
		String apiKey,
		Duration intervalo,
		boolean agendamentoHabilitado) {

	public boolean possuiChave() {
		return apiKey != null && !apiKey.isBlank();
	}

}
