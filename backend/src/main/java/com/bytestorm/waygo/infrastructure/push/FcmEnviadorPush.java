package com.bytestorm.waygo.infrastructure.push;

import java.util.Map;
import java.util.Set;

import com.bytestorm.waygo.domain.interfaces.EnviadorPush;
import com.bytestorm.waygo.domain.valueobject.ResultadoEnvioPush;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FcmEnviadorPush implements EnviadorPush {

	static final String CANAL_ANDROID = "waygo-alertas";

	private static final Set<MessagingErrorCode> TOKEN_INVALIDO = Set.of(MessagingErrorCode.UNREGISTERED,
			MessagingErrorCode.INVALID_ARGUMENT, MessagingErrorCode.SENDER_ID_MISMATCH);

	private final FirebaseMessaging messaging;

	public FcmEnviadorPush(FirebaseMessaging messaging) {
		this.messaging = messaging;
	}

	@Override
	public ResultadoEnvioPush enviar(String tokenFcm, String titulo, String corpo, Map<String, String> dados) {
		Message mensagem = Message.builder()
				.setToken(tokenFcm)
				.setNotification(Notification.builder().setTitle(titulo).setBody(corpo).build())
				.putAllData(dados)
				.setAndroidConfig(AndroidConfig.builder()
						.setPriority(AndroidConfig.Priority.HIGH)
						.setNotification(AndroidNotification.builder().setChannelId(CANAL_ANDROID).build())
						.build())
				.build();
		try {
			messaging.send(mensagem);
			return ResultadoEnvioPush.sucesso();
		} catch (FirebaseMessagingException e) {
			MessagingErrorCode codigo = e.getMessagingErrorCode();
			String detalhe = (codigo == null ? "ERRO" : codigo.name()) + ": " + e.getMessage();
			return codigo != null && TOKEN_INVALIDO.contains(codigo)
					? ResultadoEnvioPush.tokenInvalido(detalhe)
					: ResultadoEnvioPush.falhaTemporaria(detalhe);
		} catch (RuntimeException e) {
			log.warn("Erro inesperado ao enviar push pelo FCM: {}", e.getMessage());
			return ResultadoEnvioPush.falhaTemporaria(e.getMessage());
		}
	}

}
