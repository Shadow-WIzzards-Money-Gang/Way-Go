package com.bytestorm.waygo.application.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bytestorm.waygo.domain.interfaces.BaldeacaoRepository;
import com.bytestorm.waygo.domain.interfaces.ConexaoEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;
import com.bytestorm.waygo.domain.valueobject.GrafoRede;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GrafoRedeCache {

	private final LinhaTransporteRepository linhaRepository;
	private final EstacaoRepository estacaoRepository;
	private final LinhaEstacaoRepository linhaEstacaoRepository;
	private final ConexaoEstacaoRepository conexaoRepository;
	private final BaldeacaoRepository baldeacaoRepository;
	private final Clock clock;
	private final Duration intervaloRecarga;

	private volatile GrafoRede grafo;
	private volatile Instant carregadoEm;

	public GrafoRedeCache(LinhaTransporteRepository linhaRepository, EstacaoRepository estacaoRepository,
			LinhaEstacaoRepository linhaEstacaoRepository, ConexaoEstacaoRepository conexaoRepository,
			BaldeacaoRepository baldeacaoRepository, Clock clock,
			@Value("${rotas.recarga-grafo:PT6H}") Duration intervaloRecarga) {
		this.linhaRepository = linhaRepository;
		this.estacaoRepository = estacaoRepository;
		this.linhaEstacaoRepository = linhaEstacaoRepository;
		this.conexaoRepository = conexaoRepository;
		this.baldeacaoRepository = baldeacaoRepository;
		this.clock = clock;
		this.intervaloRecarga = intervaloRecarga;
	}

	public synchronized void invalidar() {
		grafo = null;
	}

	public synchronized GrafoRede obter() {
		Instant agora = clock.instant();
		if (grafo == null || !agora.isBefore(carregadoEm.plus(intervaloRecarga))) {
			grafo = GrafoRede.montar(linhaRepository.listarTodas(), estacaoRepository.listarTodas(),
					linhaEstacaoRepository.listarTodas(), conexaoRepository.listarAtivas(),
					baldeacaoRepository.listarAtivas());
			carregadoEm = agora;
			log.info("Grafo da rede carregado do banco: {} nós", grafo.quantidadeNos());
		}
		return grafo;
	}

}
