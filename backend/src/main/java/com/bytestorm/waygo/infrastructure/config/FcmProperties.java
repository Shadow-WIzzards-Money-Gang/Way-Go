package com.bytestorm.waygo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fcm")
public record FcmProperties(boolean habilitado, String credenciais) {
}
