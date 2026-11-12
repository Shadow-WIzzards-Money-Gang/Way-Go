package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import com.bytestorm.waygo.domain.enums.DiaSemana;

import lombok.Getter;

@Getter
public class RotaAgendada {

	// Minutos desde a meia-noite: de 00:00 (0) a 23:59 (1439).
	private static final int HORARIO_PARTIDA_MIN = 0;
	private static final int HORARIO_PARTIDA_MAX = 1439;
	private static final int ANTECEDENCIA_MIN = 0;
	private static final int ANTECEDENCIA_MAX = 1440;

	private static final int ANTECEDENCIA_PADRAO = 5;
	private static final Set<DiaSemana> DIAS_SEMANA_PADRAO = Set.of(DiaSemana.SEGUNDA, DiaSemana.TERCA,
			DiaSemana.QUARTA, DiaSemana.QUINTA, DiaSemana.SEXTA);

	private final Long id;
	private final Long idRotaSalva;
	private final LocalDateTime criadoEm;
	private int horarioPartidaMinutos;
	private int antecedenciaMinutos;
	private boolean ativa;
	private final Set<DiaSemana> diasSemana;

	private RotaAgendada(Long id, Long idRotaSalva, int horarioPartidaMinutos, int antecedenciaMinutos, boolean ativa,
			Set<DiaSemana> diasSemana, LocalDateTime criadoEm) {
		this.id = id;
		this.idRotaSalva = exigir(idRotaSalva, "idRotaSalva");
		this.horarioPartidaMinutos = validarHorarioPartida(horarioPartidaMinutos);
		this.antecedenciaMinutos = validarAntecedencia(antecedenciaMinutos);
		this.ativa = ativa;
		this.diasSemana = validarDiasSemana(diasSemana);
		this.criadoEm = exigir(criadoEm, "criadoEm");
	}

	public static RotaAgendada criar(Long idRotaSalva, int horarioPartidaMinutos, int antecedenciaMinutos,
			Set<DiaSemana> diasSemana, LocalDateTime criadoEm) {
		return reconstruir(null, idRotaSalva, horarioPartidaMinutos, antecedenciaMinutos, true, diasSemana, criadoEm);
	}

	public static RotaAgendada criar(Long idRotaSalva, int horarioPartidaMinutos, int antecedenciaMinutos,
			LocalDateTime criadoEm) {
		return criar(idRotaSalva, horarioPartidaMinutos, antecedenciaMinutos, DIAS_SEMANA_PADRAO, criadoEm);
	}

	public static RotaAgendada criar(Long idRotaSalva, int horarioPartidaMinutos, Set<DiaSemana> diasSemana,
			LocalDateTime criadoEm) {
		return criar(idRotaSalva, horarioPartidaMinutos, ANTECEDENCIA_PADRAO, diasSemana, criadoEm);
	}

	public static RotaAgendada criar(Long idRotaSalva, int horarioPartidaMinutos, LocalDateTime criadoEm) {
		return criar(idRotaSalva, horarioPartidaMinutos, ANTECEDENCIA_PADRAO, DIAS_SEMANA_PADRAO, criadoEm);
	}

	public static RotaAgendada reconstruir(Long id, Long idRotaSalva, int horarioPartidaMinutos,
			int antecedenciaMinutos, boolean ativa, Set<DiaSemana> diasSemana, LocalDateTime criadoEm) {
		return new RotaAgendada(id, idRotaSalva, horarioPartidaMinutos, antecedenciaMinutos, ativa, diasSemana,
				criadoEm);
	}

	public void ativar() {
		this.ativa = true;
	}

	public void desativar() {
		this.ativa = false;
	}

	public void atualizarHorarioPartida(int horarioPartidaMinutos) {
		this.horarioPartidaMinutos = validarHorarioPartida(horarioPartidaMinutos);
	}

	public void atualizarAntecedencia(int antecedenciaMinutos) {
		this.antecedenciaMinutos = validarAntecedencia(antecedenciaMinutos);
	}

	public void adicionarDiaSemana(DiaSemana diaSemana) {
		diasSemana.add(exigir(diaSemana, "diaSemana"));
	}

	public void removerDiaSemana(DiaSemana diaSemana) {
		if (diasSemana.contains(diaSemana) && diasSemana.size() == 1) {
			throw new IllegalStateException("a rota agendada precisa ter ao menos um dia da semana");
		}
		diasSemana.remove(diaSemana);
	}

	public boolean executaNo(DiaSemana diaSemana) {
		return diasSemana.contains(diaSemana);
	}

	public Set<DiaSemana> getDiasSemana() {
		return Set.copyOf(diasSemana);
	}

	private static int validarHorarioPartida(int horarioPartidaMinutos) {
		if (horarioPartidaMinutos < HORARIO_PARTIDA_MIN || horarioPartidaMinutos > HORARIO_PARTIDA_MAX) {
			throw new IllegalArgumentException(
					"horarioPartidaMinutos deve estar entre " + HORARIO_PARTIDA_MIN + " e " + HORARIO_PARTIDA_MAX);
		}
		return horarioPartidaMinutos;
	}

	private static int validarAntecedencia(int antecedenciaMinutos) {
		if (antecedenciaMinutos < ANTECEDENCIA_MIN || antecedenciaMinutos > ANTECEDENCIA_MAX) {
			throw new IllegalArgumentException(
					"antecedenciaMinutos deve estar entre " + ANTECEDENCIA_MIN + " e " + ANTECEDENCIA_MAX);
		}
		return antecedenciaMinutos;
	}

	private static Set<DiaSemana> validarDiasSemana(Set<DiaSemana> diasSemana) {
		if (diasSemana == null || diasSemana.isEmpty()) {
			throw new IllegalArgumentException("a rota agendada precisa ter ao menos um dia da semana");
		}
		return EnumSet.copyOf(diasSemana);
	}

}
