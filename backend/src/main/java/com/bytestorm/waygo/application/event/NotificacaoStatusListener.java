package com.bytestorm.waygo.application.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.bytestorm.waygo.application.service.NotificacaoRotaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoStatusListener {

	private final NotificacaoRotaService notificacaoRotaService;

	@Async
	@TransactionalEventListener(fallbackExecution = true)
	public void aoMudarStatus(StatusLinhaAlteradoEvent evento) {
		try {
			notificacaoRotaService.notificarMudancaStatus(evento);
		} catch (RuntimeException e) {
			log.error("Falha ao gerar alertas da mudança de status da linha {}: {}", evento.linhaId(),
					e.getMessage(), e);
		}
	}

}
