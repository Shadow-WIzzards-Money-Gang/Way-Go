package com.bytestorm.waygo.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.util.Horarios;

public record RotaAgendadaResponse(
		Long id,
		Long idRotaSalva,
		String horario,
		int horarioPartidaMinutos,
		int antecedenciaMinutos,
		List<DiaSemana> diasSemana,
		boolean ativa,
		LocalDateTime criadoEm) {

	public static RotaAgendadaResponse de(RotaAgendada agendamento) {
		return new RotaAgendadaResponse(
				agendamento.getId(),
				agendamento.getIdRotaSalva(),
				Horarios.formatar(agendamento.getHorarioPartidaMinutos()),
				agendamento.getHorarioPartidaMinutos(),
				agendamento.getAntecedenciaMinutos(),
				agendamento.getDiasSemana().stream().sorted().toList(),
				agendamento.isAtiva(),
				agendamento.getCriadoEm());
	}

}
