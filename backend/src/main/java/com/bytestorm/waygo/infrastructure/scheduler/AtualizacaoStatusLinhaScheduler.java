package com.bytestorm.waygo.infrastructure.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bytestorm.waygo.application.service.AtualizacaoStatusLinhaService;
import com.bytestorm.waygo.infrastructure.config.TrilhosAgoraProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "trilhos-agora.agendamento-habilitado", havingValue = "true", matchIfMissing = true)
public class AtualizacaoStatusLinhaScheduler {

	private final AtualizacaoStatusLinhaService atualizacaoStatusLinhaService;
	private final TrilhosAgoraProperties properties;

	@Scheduled(fixedDelayString = "${trilhos-agora.intervalo}", initialDelayString = "PT15S")
	public void executar() {
		if (!properties.possuiChave()) {
			log.warn("Chave da API Trilhos Agora não configurada (TRILHOS_AGORA_API_KEY); "
					+ "atualização de status ignorada");
			return;
		}
		try {
			atualizacaoStatusLinhaService.atualizar();
		} catch (Exception e) {
			// Mantém o último status registrado; a próxima execução tenta novamente.
			log.error("Falha ao atualizar o status das linhas pela Trilhos Agora: {}", e.getMessage(), e);
		}
	}

}
