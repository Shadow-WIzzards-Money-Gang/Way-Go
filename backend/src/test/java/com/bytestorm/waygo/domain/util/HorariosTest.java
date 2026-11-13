package com.bytestorm.waygo.domain.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HorariosTest {

	@Test
	void converteHorarioEmMinutosEVolta() {
		assertThat(Horarios.paraMinutos("00:00")).isZero();
		assertThat(Horarios.paraMinutos("07:10")).isEqualTo(430);
		assertThat(Horarios.paraMinutos(" 23:59 ")).isEqualTo(1439);
		assertThat(Horarios.formatar(430)).isEqualTo("07:10");
		assertThat(Horarios.formatar(0)).isEqualTo("00:00");
	}

	@Test
	void rejeitaFormatosInvalidos() {
		for (String invalido : new String[] { "24:00", "7:10", "07:60", "0710", "", "ab:cd" }) {
			assertThatThrownBy(() -> Horarios.paraMinutos(invalido))
					.as(invalido)
					.isInstanceOf(IllegalArgumentException.class);
		}
		assertThatThrownBy(() -> Horarios.paraMinutos(null)).isInstanceOf(IllegalArgumentException.class);
	}

}
