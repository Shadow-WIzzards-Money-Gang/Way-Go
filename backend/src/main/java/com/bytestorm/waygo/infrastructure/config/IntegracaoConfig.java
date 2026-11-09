package com.bytestorm.waygo.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.bytestorm.waygo.infrastructure.client.trilhosagora.TrilhosAgoraClient;

@Configuration
@EnableScheduling
@EnableFeignClients(basePackageClasses = TrilhosAgoraClient.class)
@EnableConfigurationProperties(TrilhosAgoraProperties.class)
public class IntegracaoConfig {

	public static final ZoneId FUSO_SAO_PAULO = ZoneId.of("America/Sao_Paulo");

	@Bean
	public Clock clock() {
		return Clock.system(FUSO_SAO_PAULO);
	}

}
