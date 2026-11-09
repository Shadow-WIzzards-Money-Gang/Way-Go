package com.bytestorm.waygo.infrastructure.client.trilhosagora;

import org.springframework.context.annotation.Bean;

import com.bytestorm.waygo.infrastructure.config.TrilhosAgoraProperties;

import feign.RequestInterceptor;

public class TrilhosAgoraFeignConfig {

	static final String HEADER_CHAVE = "X-API-Key";

	@Bean
	public RequestInterceptor chaveTrilhosAgoraInterceptor(TrilhosAgoraProperties properties) {
		return template -> template.header(HEADER_CHAVE, properties.apiKey());
	}

}
