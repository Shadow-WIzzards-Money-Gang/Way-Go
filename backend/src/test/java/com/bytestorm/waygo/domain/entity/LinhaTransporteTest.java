package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.enums.TipoTransporte;

class LinhaTransporteTest {

	private LinhaTransporte linha() {
		return LinhaTransporte.criar("Linha 11 - Coral", "11", "#FF7F50", "CPTM", TipoTransporte.TREM_METROPOLITANO);
	}

	@Test
	void criarGeraLinhaNovaAtivaESemId() {
		LinhaTransporte linha = linha();

		assertThat(linha.getId()).isNull();
		assertThat(linha.isAtiva()).isTrue();
		assertThat(linha.getCodigo()).isEqualTo("11");
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		LinhaTransporte linha = LinhaTransporte.reconstruir(7L, "Linha 7 - Rubi", "7", "#A8034F", "TIC Trens",
				TipoTransporte.TREM_METROPOLITANO, false);

		assertThat(linha.getId()).isEqualTo(7L);
		assertThat(linha.getNome()).isEqualTo("Linha 7 - Rubi");
		assertThat(linha.getCor()).isEqualTo("#A8034F");
		assertThat(linha.getOperadora()).isEqualTo("TIC Trens");
		assertThat(linha.getTipoTransporte()).isEqualTo(TipoTransporte.TREM_METROPOLITANO);
		assertThat(linha.isAtiva()).isFalse();
	}

	@Test
	void ativarEDesativarSaoIdempotentes() {
		LinhaTransporte linha = linha();

		linha.desativar();
		linha.desativar();
		assertThat(linha.isAtiva()).isFalse();

		linha.ativar();
		linha.ativar();
		assertThat(linha.isAtiva()).isTrue();
	}

	@Test
	void corEOpcionalMasPrecisaSerHex() {
		assertThat(LinhaTransporte.criar("Expresso", "exp", null, "Trivia", TipoTransporte.TREM_METROPOLITANO)
				.getCor()).isNull();

		LinhaTransporte linha = linha();
		assertThatThrownBy(() -> linha.alterarCor("laranja")).isInstanceOf(IllegalArgumentException.class);

		linha.alterarCor("#1266af");
		assertThat(linha.getCor()).isEqualTo("#1266af");
	}

	@Test
	void rejeitaCamposObrigatoriosInvalidos() {
		assertThatThrownBy(() -> LinhaTransporte.criar(" ", "11", null, "CPTM", TipoTransporte.METRO))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> LinhaTransporte.criar("Linha", "11", null, "CPTM", null))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> linha().renomear("x".repeat(101))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void comparaCodigoSemDiferenciarMaiusculas() {
		LinhaTransporte linha = LinhaTransporte.criar("Expresso Aeroporto", "expresso-aeroporto", null, "Trivia",
				TipoTransporte.TREM_METROPOLITANO);

		assertThat(linha.possuiCodigo(" Expresso-Aeroporto ")).isTrue();
		assertThat(linha.possuiCodigo("11")).isFalse();
	}

}
