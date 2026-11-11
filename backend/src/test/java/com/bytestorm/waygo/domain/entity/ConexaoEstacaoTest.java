package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConexaoEstacaoTest {

	@Test
	void criarGeraTrechoAtivoSemId() {
		ConexaoEstacao conexao = ConexaoEstacao.criar(1L, 2L, 120, 1500);

		assertThat(conexao.getId()).isNull();
		assertThat(conexao.isAtiva()).isTrue();
		assertThat(conexao.podeSerPercorrida()).isTrue();
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		ConexaoEstacao conexao = ConexaoEstacao.reconstruir(9L, 1L, 2L, 90, 800, false);

		assertThat(conexao.getId()).isEqualTo(9L);
		assertThat(conexao.getLinhaEstacaoOrigemId()).isEqualTo(1L);
		assertThat(conexao.getLinhaEstacaoDestinoId()).isEqualTo(2L);
		assertThat(conexao.getTempoViagemSegundos()).isEqualTo(90);
		assertThat(conexao.getDistanciaMetros()).isEqualTo(800);
		assertThat(conexao.isAtiva()).isFalse();
	}

	@Test
	void soPodeSerPercorridaSeAtivaEComTempoConhecido() {
		ConexaoEstacao semTempo = ConexaoEstacao.criar(1L, 2L, null, null);
		assertThat(semTempo.podeSerPercorrida()).isFalse();

		ConexaoEstacao conexao = ConexaoEstacao.criar(1L, 2L, 60, null);
		conexao.desativar();
		conexao.desativar();
		assertThat(conexao.podeSerPercorrida()).isFalse();

		conexao.ativar();
		assertThat(conexao.podeSerPercorrida()).isTrue();
	}

	@Test
	void tempoEDistanciaPrecisamSerPositivos() {
		assertThatThrownBy(() -> ConexaoEstacao.criar(1L, 2L, 0, null)).isInstanceOf(IllegalArgumentException.class);

		ConexaoEstacao conexao = ConexaoEstacao.criar(1L, 2L, 60, 100);
		assertThatThrownBy(() -> conexao.atualizarDistancia(-5)).isInstanceOf(IllegalArgumentException.class);

		conexao.atualizarTempoViagem(75);
		assertThat(conexao.getTempoViagemSegundos()).isEqualTo(75);
	}

	@Test
	void origemEDestinoPrecisamSerDiferentes() {
		assertThatThrownBy(() -> ConexaoEstacao.criar(1L, 1L, 60, 100))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
