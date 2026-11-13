package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
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

/** O status do administrador vale como um da API: a próxima mudança da API o substitui. */
class AtualizacaoStatusLinhaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-13T12:00:00Z");
	private static final LocalDateTime AGORA_LOCAL = LocalDateTime.ofInstant(AGORA, ZoneOffset.UTC);

	private final RedeEmMemoria rede = new RedeEmMemoria();
	private final Long linha = rede.linhas.salvar(
			LinhaTransporte.criar("Linha 1-Azul", "1", "#0455A1", "Metrô", TipoTransporte.METRO)).getId();

	private int atualizarCom(StatusOperacional statusDaApi) {
		Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
		List<StatusLinhaExterno> api = List.of(new StatusLinhaExterno("1", statusDaApi, null));
		return new AtualizacaoStatusLinhaService(() -> api, rede.status, rede.statusCache(clock), clock)
				.atualizar();
	}

	@Test
	void apiSubstituiStatusRecenteDoAdministradorQuandoDiverge() {
		rede.status.salvar(LinhaStatus.registrarPeloAdministrador(linha, StatusOperacional.PARALISADA, null,
				AGORA_LOCAL.minusMinutes(1)));

		assertThat(atualizarCom(StatusOperacional.NORMAL)).isEqualTo(1);
		LinhaStatus atual = rede.status.buscarAtualPorLinha(linha).orElseThrow();
		assertThat(atual.getOrigem()).isEqualTo(OrigemStatus.API);
		assertThat(atual.getStatus()).isEqualTo(StatusOperacional.NORMAL);
	}

	@Test
	void naoRegistraDeNovoQuandoAApiConcordaComOStatusAtual() {
		rede.status.salvar(LinhaStatus.registrarPeloAdministrador(linha, StatusOperacional.PARALISADA, null,
				AGORA_LOCAL.minusMinutes(1)));

		assertThat(atualizarCom(StatusOperacional.PARALISADA)).isZero();
		assertThat(rede.status.buscarAtualPorLinha(linha).orElseThrow().getOrigem())
				.isEqualTo(OrigemStatus.ADMINISTRADOR);
	}

}
