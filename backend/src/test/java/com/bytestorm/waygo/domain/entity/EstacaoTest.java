package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.valueobject.Coordenada;

class EstacaoTest {

	@Test
	void criarGeraEstacaoAtivaSemIdECoordenadaOpcional() {
		Estacao estacao = Estacao.criar("Brás", null);

		assertThat(estacao.getId()).isNull();
		assertThat(estacao.isAtiva()).isTrue();
		assertThat(estacao.possuiCoordenada()).isFalse();
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		Coordenada coordenada = new Coordenada(new BigDecimal("-23.5"), new BigDecimal("-46.6"));
		Estacao estacao = Estacao.reconstruir(3L, "Luz", coordenada, false);

		assertThat(estacao.getId()).isEqualTo(3L);
		assertThat(estacao.getNome()).isEqualTo("Luz");
		assertThat(estacao.getCoordenada()).isEqualTo(coordenada);
		assertThat(estacao.isAtiva()).isFalse();
	}

	@Test
	void ativarEDesativarSaoIdempotentes() {
		Estacao estacao = Estacao.criar("Sé", null);

		estacao.desativar();
		estacao.desativar();
		assertThat(estacao.isAtiva()).isFalse();

		estacao.ativar();
		assertThat(estacao.isAtiva()).isTrue();
	}

	@Test
	void comparaNomeIgnorandoAcentosEMaiusculas() {
		Estacao estacao = Estacao.criar("José Bonifácio", null);

		assertThat(estacao.possuiNome("jose  bonifacio ")).isTrue();
		assertThat(estacao.possuiNome("Guaianases")).isFalse();
		assertThat(estacao.possuiNome(null)).isFalse();
	}

	@Test
	void renomearERelocalizarValidam() {
		Estacao estacao = Estacao.criar("Luz", null);

		assertThatThrownBy(() -> estacao.renomear("")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> estacao.renomear("x".repeat(151))).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> estacao.relocalizar(null)).isInstanceOf(NullPointerException.class);

		estacao.relocalizar(new Coordenada(BigDecimal.ONE, BigDecimal.TEN));
		assertThat(estacao.possuiCoordenada()).isTrue();
	}

}
