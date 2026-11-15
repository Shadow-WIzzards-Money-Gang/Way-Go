package com.bytestorm.waygo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("email")
public record EmailProperties(
		boolean habilitado,
		String remetente,
		String nomeRemetente) {

}
