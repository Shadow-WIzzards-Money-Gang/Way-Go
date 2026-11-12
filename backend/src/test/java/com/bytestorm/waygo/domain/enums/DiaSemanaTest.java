package com.bytestorm.waygo.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;

import org.junit.jupiter.api.Test;

class DiaSemanaTest {

	@Test
	void deAceitaNomeEDescricaoSemDiferenciarAcentos() {
		assertThat(DiaSemana.de("TERCA")).isEqualTo(DiaSemana.TERCA);
		assertThat(DiaSemana.de("Terça-feira")).isEqualTo(DiaSemana.TERCA);
		assertThat(DiaSemana.de("terca-feira")).isEqualTo(DiaSemana.TERCA);
		assertThat(DiaSemana.de("sábado")).isEqualTo(DiaSemana.SABADO);
		assertThatThrownBy(() -> DiaSemana.de("feriado")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> DiaSemana.de((String) null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void converteDeEParaDayOfWeek() {
		for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
			assertThat(DiaSemana.de(dayOfWeek).toDayOfWeek()).isEqualTo(dayOfWeek);
		}
		assertThat(DiaSemana.de(DayOfWeek.MONDAY)).isEqualTo(DiaSemana.SEGUNDA);
	}

}
