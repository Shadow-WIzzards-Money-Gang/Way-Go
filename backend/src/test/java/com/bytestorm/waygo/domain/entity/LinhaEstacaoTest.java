package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LinhaEstacaoTest {

	@Test
	void criarGeraPosicaoAtivaSemId() {
		LinhaEstacao le = LinhaEstacao.criar(11L, 3L, 1);

		assertThat(le.getId()).isNull();
		assertThat(le.isAtiva()).isTrue();
		assertThat(le.pertenceALinha(11L)).isTrue();
		assertThat(le.pertenceALinha(12L)).isFalse();
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		LinhaEstacao le = LinhaEstacao.reconstruir(5L, 11L, 3L, 4, false);

		assertThat(le.getId()).isEqualTo(5L);
		assertThat(le.getLinhaId()).isEqualTo(11L);
		assertThat(le.getEstacaoId()).isEqualTo(3L);
		assertThat(le.getSequencia()).isEqualTo(4);
		assertThat(le.isAtiva()).isFalse();
	}

	@Test
	void ativarEDesativarSaoIdempotentes() {
		LinhaEstacao le = LinhaEstacao.criar(1L, 2L, 1);

		le.desativar();
		le.desativar();
		assertThat(le.isAtiva()).isFalse();

		le.ativar();
		assertThat(le.isAtiva()).isTrue();
	}

	@Test
	void sequenciaPrecisaSerPositiva() {
		assertThatThrownBy(() -> LinhaEstacao.criar(1L, 2L, 0)).isInstanceOf(IllegalArgumentException.class);

		LinhaEstacao le = LinhaEstacao.criar(1L, 2L, 1);
		assertThatThrownBy(() -> le.reposicionar(-1)).isInstanceOf(IllegalArgumentException.class);

		le.reposicionar(8);
		assertThat(le.getSequencia()).isEqualTo(8);
	}

	@Test
	void linhaEEstacaoSaoObrigatorias() {
		assertThatThrownBy(() -> LinhaEstacao.criar(null, 2L, 1)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> LinhaEstacao.criar(1L, null, 1)).isInstanceOf(NullPointerException.class);
	}

}
