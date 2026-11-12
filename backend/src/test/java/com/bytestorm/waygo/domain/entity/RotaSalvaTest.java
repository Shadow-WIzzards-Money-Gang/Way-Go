package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.enums.CriterioRota;

class RotaSalvaTest {

	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 11, 10, 0);
	private static final UUID USUARIO = UUID.randomUUID();

	private RotaSalva rota() {
		return RotaSalva.criar(1L, 2L, " Casa - Trabalho ", USUARIO, AGORA);
	}

	@Test
	void criarUsaCriterioEquilibradoPorPadraoESemId() {
		RotaSalva rota = rota();

		assertThat(rota.getId()).isNull();
		assertThat(rota.getNome()).isEqualTo("Casa - Trabalho");
		assertThat(rota.getCriterioRota()).isEqualTo(CriterioRota.EQUILIBRADO);
		assertThat(rota.getIdUsuario()).isEqualTo(USUARIO);
		assertThat(rota.getCriadoEm()).isEqualTo(AGORA);
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		RotaSalva rota = RotaSalva.reconstruir(7L, 1L, 2L, "Faculdade", CriterioRota.MENOR_TEMPO, USUARIO, AGORA);

		assertThat(rota.getId()).isEqualTo(7L);
		assertThat(rota.getIdEstacaoOrigem()).isEqualTo(1L);
		assertThat(rota.getIdEstacaoDestino()).isEqualTo(2L);
		assertThat(rota.getCriterioRota()).isEqualTo(CriterioRota.MENOR_TEMPO);
	}

	@Test
	void exigeCamposObrigatorios() {
		assertThatThrownBy(() -> RotaSalva.criar(null, 2L, "Rota", USUARIO, AGORA))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("idEstacaoOrigem");
		assertThatThrownBy(() -> RotaSalva.criar(1L, null, "Rota", USUARIO, AGORA))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("idEstacaoDestino");
		assertThatThrownBy(() -> RotaSalva.criar(1L, 2L, "Rota", null, AGORA))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> RotaSalva.criar(1L, 2L, "Rota", null, USUARIO, AGORA))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void validaNome() {
		assertThatThrownBy(() -> RotaSalva.criar(1L, 2L, "  ", USUARIO, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> RotaSalva.criar(1L, 2L, "x".repeat(101), USUARIO, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void origemEDestinoDevemSerDiferentes() {
		assertThatThrownBy(() -> RotaSalva.criar(1L, 1L, "Rota", USUARIO, AGORA))
				.isInstanceOf(IllegalArgumentException.class);

		RotaSalva rota = rota();
		assertThatThrownBy(() -> rota.atualizarEstacaoOrigem(2L)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> rota.atualizarEstacaoDestino(1L)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> rota.atualizarEstacaoOrigem(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("idEstacaoOrigem");
	}

	@Test
	void atualizaCampos() {
		RotaSalva rota = rota();

		rota.atualizarNome("Academia");
		rota.atualizarEstacaoOrigem(3L);
		rota.atualizarEstacaoDestino(4L);
		rota.atualizarCriterioRota(CriterioRota.MENOS_BALDEACOES);

		assertThat(rota.getNome()).isEqualTo("Academia");
		assertThat(rota.getIdEstacaoOrigem()).isEqualTo(3L);
		assertThat(rota.getIdEstacaoDestino()).isEqualTo(4L);
		assertThat(rota.getCriterioRota()).isEqualTo(CriterioRota.MENOS_BALDEACOES);
	}

}
