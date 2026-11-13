package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoTransporte;
import com.bytestorm.waygo.domain.valueobject.StatusLinhaExterno;

/** O status definido pelo administrador prevalece sobre a API externa durante a janela configurada. */
class AtualizacaoStatusLinhaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-13T12:00:00Z");
	private static final LocalDateTime AGORA_LOCAL = LocalDateTime.ofInstant(AGORA, ZoneOffset.UTC);
	private static final Duration JANELA = Duration.ofHours(6);

	private final RedeEmMemoria rede = new RedeEmMemoria();
	private final Long linha = rede.linhas.salvar(
			LinhaTransporte.criar("Linha 1-Azul", "1", "#0455A1", "Metrô", TipoTransporte.METRO)).getId();

	private int atualizar() {
		Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
		List<StatusLinhaExterno> api = List.of(new StatusLinhaExterno("1", StatusOperacional.NORMAL, null));
		return new AtualizacaoStatusLinhaService(() -> api, rede.status, rede.statusCache(clock), clock, JANELA)
				.atualizar();
	}

	@Test
	void naoSobrescreveStatusRecenteDoAdministrador() {
		rede.status.salvar(LinhaStatus.registrarPeloAdministrador(linha, StatusOperacional.PARALISADA, null,
				AGORA_LOCAL.minusHours(1)));

		assertThat(atualizar()).isZero();
		assertThat(rede.status.buscarAtualPorLinha(linha).orElseThrow().getOrigem())
				.isEqualTo(OrigemStatus.ADMINISTRADOR);
	}

	@Test
	void apiVoltaAMandarDepoisDaJanela() {
		rede.status.salvar(LinhaStatus.registrarPeloAdministrador(linha, StatusOperacional.PARALISADA, null,
				AGORA_LOCAL.minus(JANELA).minusMinutes(1)));

		assertThat(atualizar()).isEqualTo(1);
		LinhaStatus atual = rede.status.buscarAtualPorLinha(linha).orElseThrow();
		assertThat(atual.getOrigem()).isEqualTo(OrigemStatus.API);
		assertThat(atual.getStatus()).isEqualTo(StatusOperacional.NORMAL);
	}

	@Test
	void statusAnteriorDaApiEhSubstituidoNormalmente() {
		rede.status.salvar(LinhaStatus.registrarPelaApi(linha, StatusOperacional.PARALISADA, null, AGORA_LOCAL));

		assertThat(atualizar()).isEqualTo(1);
	}

}
