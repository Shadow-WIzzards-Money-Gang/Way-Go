package com.bytestorm.waygo.application.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StatusLinhasCache {

	private final LinhaTransporteRepository linhaRepository;
	private final LinhaStatusRepository linhaStatusRepository;
	private final Clock clock;
	private final Duration intervaloRecarga;

	private final Map<String, Long> linhaIdPorCodigo = new ConcurrentHashMap<>();
	private final Map<Long, StatusOperacional> statusPorLinha = new ConcurrentHashMap<>();
	private volatile Instant carregadoEm;

	public StatusLinhasCache(LinhaTransporteRepository linhaRepository, LinhaStatusRepository linhaStatusRepository,
			Clock clock, @Value("${trilhos-agora.recarga-cache:PT30M}") Duration intervaloRecarga) {
		this.linhaRepository = linhaRepository;
		this.linhaStatusRepository = linhaStatusRepository;
		this.clock = clock;
		this.intervaloRecarga = intervaloRecarga;
	}

	public synchronized void carregarSeNecessario() {
		Instant agora = clock.instant();
		if (carregadoEm != null && agora.isBefore(carregadoEm.plus(intervaloRecarga))) {
			return;
		}

		linhaIdPorCodigo.clear();
		statusPorLinha.clear();
		for (LinhaTransporte linha : linhaRepository.listarTodas()) {
			linhaIdPorCodigo.put(linha.getCodigo(), linha.getId());
		}
		for (LinhaStatus status : linhaStatusRepository.buscarAtuaisDeTodasAsLinhas()) {
			statusPorLinha.put(status.getLinhaId(), status.getStatus());
		}
		carregadoEm = agora;
		log.debug("Cache de status carregado do banco: {} linhas", linhaIdPorCodigo.size());
	}

	public synchronized void invalidar() {
		carregadoEm = null;
	}

	public Optional<Long> linhaIdPorCodigo(String codigo) {
		return Optional.ofNullable(linhaIdPorCodigo.get(codigo));
	}

	public Optional<StatusOperacional> statusAtual(Long linhaId) {
		return Optional.ofNullable(statusPorLinha.get(linhaId));
	}

	/** Cópia do status atual de todas as linhas, carregando do banco se o cache estiver vazio ou expirado. */
	public synchronized Map<Long, StatusOperacional> statusAtuais() {
		carregarSeNecessario();
		return Map.copyOf(statusPorLinha);
	}

	public void registrar(Long linhaId, StatusOperacional status) {
		statusPorLinha.put(linhaId, status);
	}

}
