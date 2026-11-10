package com.bytestorm.waygo.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	public static final String API_PREFIXO = "/api/v1";

	/** Origens liberadas para chamadas do navegador (ex.: app Expo rodando na web). */
	private final String[] origensPermitidas;

	public WebConfig(@Value("${cors.origens-permitidas:}") String[] origensPermitidas) {
		this.origensPermitidas = origensPermitidas;
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(API_PREFIXO, HandlerTypePredicate.forAnnotation(RestController.class));
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		if (origensPermitidas.length == 0) {
			return;
		}
		registry.addMapping(API_PREFIXO + "/**")
				.allowedOriginPatterns(origensPermitidas)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
	}

}
