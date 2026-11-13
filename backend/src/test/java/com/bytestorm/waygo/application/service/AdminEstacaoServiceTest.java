package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.request.EstacaoRequest;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;

class AdminEstacaoServiceTest {

	private static final BigDecimal LAT = new BigDecimal("-23.5475");
	private static final BigDecimal LON = new BigDecimal("-46.6522");

	private final RedeEmMemoria rede = new RedeEmMemoria();
	private final AdminEstacaoService service = new AdminEstacaoService(rede.estacoes,
			rede.grafoCache(Clock.systemUTC()));

	@Test
	void criaEstacaoComPosicao() {
		EstacaoResumoResponse estacao = service.criar(new EstacaoRequest("Higienópolis", LAT, LON, null));

		assertThat(estacao.id()).isNotNull();
		assertThat(estacao.latitude()).isEqualTo(LAT);
		assertThat(estacao.ativa()).isTrue();
	}

	@Test
	void nomeEObrigatorioNaCriacao() {
		assertThatThrownBy(() -> service.criar(new EstacaoRequest(" ", LAT, LON, null)))
				.isInstanceOf(RequisicaoInvalidaException.class);
	}

	@Test
	void latitudeELongitudeAndamJuntasEDentroDaFaixa() {
		assertThatThrownBy(() -> service.criar(new EstacaoRequest("X", LAT, null, null)))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.criar(new EstacaoRequest("X", new BigDecimal("91"), LON, null)))
				.isInstanceOf(RequisicaoInvalidaException.class);
	}

	@Test
	void relocalizaSemMudarONome() {
		Long id = service.criar(new EstacaoRequest("Higienópolis", LAT, LON, null)).id();
		BigDecimal novaLat = new BigDecimal("-23.5500");

		EstacaoResumoResponse movida = service.atualizar(id, new EstacaoRequest(null, novaLat, LON, null));

		assertThat(movida.nome()).isEqualTo("Higienópolis");
		assertThat(movida.latitude()).isEqualTo(novaLat);
	}

	@Test
	void desativarMantemAEstacaoNoBanco() {
		Long id = service.criar(new EstacaoRequest("Higienópolis", LAT, LON, null)).id();

		service.desativar(id);

		assertThat(rede.estacoes.buscarPorId(id).orElseThrow().isAtiva()).isFalse();
	}

}
