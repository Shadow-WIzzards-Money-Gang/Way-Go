package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BaldeacaoTest {

	@Test
	void criarGeraBaldeacaoAtivaSemId() {
		Baldeacao baldeacao = Baldeacao.criar(1L, 2L, 180);

		assertThat(baldeacao.getId()).isNull();
		assertThat(baldeacao.isAtiva()).isTrue();
		assertThat(baldeacao.podeSerUtilizada()).isTrue();
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		Baldeacao baldeacao = Baldeacao.reconstruir(4L, 1L, 2L, 240, false);

		assertThat(baldeacao.getId()).isEqualTo(4L);
		assertThat(baldeacao.getLinhaEstacaoOrigemId()).isEqualTo(1L);
		assertThat(baldeacao.getLinhaEstacaoDestinoId()).isEqualTo(2L);
		assertThat(baldeacao.getTempoBaldeacaoSegundos()).isEqualTo(240);
		assertThat(baldeacao.isAtiva()).isFalse();
	}

	@Test
	void soPodeSerUtilizadaSeAtivaEComTempoConhecido() {
		assertThat(Baldeacao.criar(1L, 2L, null).podeSerUtilizada()).isFalse();

		Baldeacao baldeacao = Baldeacao.criar(1L, 2L, 60);
		baldeacao.desativar();
		baldeacao.desativar();
		assertThat(baldeacao.podeSerUtilizada()).isFalse();

		baldeacao.ativar();
		assertThat(baldeacao.podeSerUtilizada()).isTrue();
	}

	@Test
	void validaTempoEPontas() {
		assertThatThrownBy(() -> Baldeacao.criar(1L, 1L, 60)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Baldeacao.criar(1L, 2L, 0)).isInstanceOf(IllegalArgumentException.class);

		Baldeacao baldeacao = Baldeacao.criar(1L, 2L, 60);
		baldeacao.atualizarTempo(90);
		assertThat(baldeacao.getTempoBaldeacaoSegundos()).isEqualTo(90);
	}

}
