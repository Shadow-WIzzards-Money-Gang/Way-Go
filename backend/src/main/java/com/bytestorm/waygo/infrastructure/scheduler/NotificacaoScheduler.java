package com.bytestorm.waygo.infrastructure.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bytestorm.waygo.application.service.EnvioNotificacaoService;
import com.bytestorm.waygo.application.service.NotificacaoRotaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notificacoes.agendamento-habilitado", havingValue = "true", matchIfMissing = true)
public class NotificacaoScheduler {

	private final NotificacaoRotaService notificacaoRotaService;
	private final EnvioNotificacaoService envioNotificacaoService;

	@Scheduled(fixedDelayString = "${notificacoes.intervalo-lembretes:PT1M}", initialDelayString = "PT20S")
	public void enfileirarLembretes() {
		try {
			notificacaoRotaService.enfileirarLembretes();
		} catch (Exception e) {
			log.error("Falha ao verificar os lembretes de agendamento: {}", e.getMessage(), e);
		}
	}

	@Scheduled(fixedDelayString = "${notificacoes.intervalo-fila:PT30S}", initialDelayString = "PT30S")
	public void processarFila() {
		try {
			envioNotificacaoService.processarFila();
		} catch (Exception e) {
			log.error("Falha ao processar a fila de notificações: {}", e.getMessage(), e);
		}
	}

}
