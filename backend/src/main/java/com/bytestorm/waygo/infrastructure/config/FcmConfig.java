package com.bytestorm.waygo.infrastructure.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bytestorm.waygo.domain.interfaces.EnviadorPush;
import com.bytestorm.waygo.infrastructure.push.FcmEnviadorPush;
import com.bytestorm.waygo.infrastructure.push.LogEnviadorPush;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
@EnableConfigurationProperties(FcmProperties.class)
public class FcmConfig {

	private static final String NOME_APP = "waygo";

	@Bean
	@ConditionalOnProperty(name = "fcm.habilitado", havingValue = "true")
	public FirebaseMessaging firebaseMessaging(FcmProperties propriedades) throws IOException {
		FirebaseApp app = FirebaseApp.getApps().stream()
				.filter(a -> a.getName().equals(NOME_APP))
				.findFirst()
				.orElse(null);
		if (app == null) {
			try (InputStream credenciais = abrirCredenciais(propriedades.credenciais())) {
				FirebaseOptions opcoes = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(credenciais))
						.build();
				app = FirebaseApp.initializeApp(opcoes, NOME_APP);
			}
		}
		return FirebaseMessaging.getInstance(app);
	}

	@Bean
	@ConditionalOnProperty(name = "fcm.habilitado", havingValue = "true")
	public EnviadorPush fcmEnviadorPush(FirebaseMessaging firebaseMessaging) {
		return new FcmEnviadorPush(firebaseMessaging);
	}

	@Bean
	@ConditionalOnProperty(name = "fcm.habilitado", havingValue = "false", matchIfMissing = true)
	public EnviadorPush logEnviadorPush() {
		return new LogEnviadorPush();
	}

	static InputStream abrirCredenciais(String valor) throws IOException {
		if (valor == null || valor.isBlank()) {
			throw new IllegalStateException(
					"FCM_HABILITADO=true, mas FCM_CREDENCIAIS (conta de serviço do Firebase) não foi informada");
		}
		String texto = valor.trim();
		if (texto.startsWith("{")) {
			return new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8));
		}
		Path caminho = Path.of(texto);
		if (Files.isRegularFile(caminho)) {
			return new FileInputStream(caminho.toFile());
		}
		try {
			return new ByteArrayInputStream(Base64.getDecoder().decode(texto));
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
					"FCM_CREDENCIAIS não é um arquivo existente, nem JSON, nem base64 válido", e);
		}
	}

}
