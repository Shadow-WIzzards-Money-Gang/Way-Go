package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.enums.DiaSemana;

class RotaAgendadaTest {

	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 11, 10, 0);

	@Test
	void criarUsaDiasUteisEAntecedenciaPadrao() {
		RotaAgendada agendada = RotaAgendada.criar(1L, 430, AGORA);

		assertThat(agendada.getId()).isNull();
		assertThat(agendada.getHorarioPartidaMinutos()).isEqualTo(430);
		assertThat(agendada.getAntecedenciaMinutos()).isEqualTo(5);
		assertThat(agendada.isAtiva()).isTrue();
		assertThat(agendada.getDiasSemana()).containsExactlyInAnyOrder(DiaSemana.SEGUNDA, DiaSemana.TERCA,
				DiaSemana.QUARTA, DiaSemana.QUINTA, DiaSemana.SEXTA);
		assertThat(agendada.getCriadoEm()).isEqualTo(AGORA);
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		RotaAgendada agendada = RotaAgendada.reconstruir(9L, 1L, 0, 1440, false, Set.of(DiaSemana.SABADO), AGORA);

		assertThat(agendada.getId()).isEqualTo(9L);
		assertThat(agendada.getIdRotaSalva()).isEqualTo(1L);
		assertThat(agendada.getHorarioPartidaMinutos()).isZero();
		assertThat(agendada.getAntecedenciaMinutos()).isEqualTo(1440);
		assertThat(agendada.isAtiva()).isFalse();
		assertThat(agendada.getDiasSemana()).containsExactly(DiaSemana.SABADO);
	}

	@Test
	void validaFaixasDoBanco() {
		assertThatThrownBy(() -> RotaAgendada.criar(1L, -1, AGORA)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> RotaAgendada.criar(1L, 1440, AGORA)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> RotaAgendada.criar(1L, 430, -1, AGORA)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> RotaAgendada.criar(1L, 430, 1441, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void exigeRotaSalvaEAoMenosUmDia() {
		assertThatThrownBy(() -> RotaAgendada.criar(null, 430, AGORA)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> RotaAgendada.criar(1L, 430, Set.of(), AGORA))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> RotaAgendada.criar(1L, 430, (Set<DiaSemana>) null, AGORA))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void naoRemoveOUltimoDia() {
		RotaAgendada agendada = RotaAgendada.criar(1L, 430, Set.of(DiaSemana.SEGUNDA), AGORA);

		assertThatThrownBy(() -> agendada.removerDiaSemana(DiaSemana.SEGUNDA))
				.isInstanceOf(IllegalStateException.class);

		agendada.adicionarDiaSemana(DiaSemana.QUARTA);
		agendada.removerDiaSemana(DiaSemana.SEGUNDA);

		assertThat(agendada.getDiasSemana()).containsExactly(DiaSemana.QUARTA);
		assertThat(agendada.executaNo(DiaSemana.QUARTA)).isTrue();
		assertThat(agendada.executaNo(DiaSemana.SEGUNDA)).isFalse();
	}

	@Test
	void diasSemanaNaoPodeSerAlteradoPorFora() {
		RotaAgendada agendada = RotaAgendada.criar(1L, 430, AGORA);

		assertThatThrownBy(() -> agendada.getDiasSemana().clear())
				.isInstanceOf(UnsupportedOperationException.class);
		assertThat(agendada.getDiasSemana()).hasSize(5);
	}

	@Test
	void ativaEDesativaEAtualizaHorarios() {
		RotaAgendada agendada = RotaAgendada.criar(1L, 430, AGORA);

		agendada.desativar();
		assertThat(agendada.isAtiva()).isFalse();
		agendada.ativar();
		assertThat(agendada.isAtiva()).isTrue();

		agendada.atualizarHorarioPartida(1439);
		agendada.atualizarAntecedencia(0);
		assertThat(agendada.getHorarioPartidaMinutos()).isEqualTo(1439);
		assertThat(agendada.getAntecedenciaMinutos()).isZero();
		assertThatThrownBy(() -> agendada.atualizarHorarioPartida(1440))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
