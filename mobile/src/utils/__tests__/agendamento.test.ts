import { describe, expect, it } from '@jest/globals';

import type { RotaAgendada } from '@/types/api';

import {
  descreverAntecedencia,
  diaParaIndice,
  indicesParaDias,
  mascararHorario,
  momentoDoLembrete,
  resumoAgendamentos,
} from '../agendamento';

const agendamento = (parcial: Partial<RotaAgendada>): RotaAgendada => ({
  id: 1,
  idRotaSalva: 1,
  horario: '07:00',
  horarioPartidaMinutos: 420,
  antecedenciaMinutos: 5,
  diasSemana: ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA'],
  ativa: true,
  criadoEm: '2026-11-13T12:00:00',
  ...parcial,
});

describe('dias da semana', () => {
  it('converte entre o formato da API e o índice (0 = domingo)', () => {
    expect(diaParaIndice('DOMINGO')).toBe(0);
    expect(diaParaIndice('SABADO')).toBe(6);
    expect(indicesParaDias([5, 1, 1, 3])).toEqual(['SEGUNDA', 'QUARTA', 'SEXTA']);
  });
});

describe('resumoAgendamentos', () => {
  it('descreve nenhum, um e vários agendamentos', () => {
    expect(resumoAgendamentos([])).toBe('Sem agendamento');
    expect(resumoAgendamentos([agendamento({})])).toBe('Seg a Sex · 07:00');
    expect(
      resumoAgendamentos([
        agendamento({ id: 2, horario: '18:00', horarioPartidaMinutos: 1080 }),
        agendamento({ id: 1 }),
        agendamento({
          id: 3,
          horario: '09:00',
          horarioPartidaMinutos: 540,
          diasSemana: ['SABADO'],
        }),
      ]),
    ).toBe('Seg a Sex · 07:00 e mais 2 horários');
  });

  it('ignora os pausados e avisa quando todos estão pausados', () => {
    expect(
      resumoAgendamentos([agendamento({ ativa: false }), agendamento({ id: 2, horario: '18:00' })]),
    ).toBe('Seg a Sex · 18:00');
    expect(resumoAgendamentos([agendamento({ ativa: false })])).toBe('Agendamento pausado');
  });
});

describe('momentoDoLembrete', () => {
  it('desconta a antecedência', () => {
    expect(momentoDoLembrete(1, 430, 10)).toEqual({ dia: 1, hora: 7, minuto: 0 });
    expect(momentoDoLembrete(3, 420, 0)).toEqual({ dia: 3, hora: 7, minuto: 0 });
  });

  it('volta para o dia anterior ao passar da meia-noite', () => {
    expect(momentoDoLembrete(1, 10, 30)).toEqual({ dia: 0, hora: 23, minuto: 40 });
    expect(momentoDoLembrete(0, 0, 1)).toEqual({ dia: 6, hora: 23, minuto: 59 });
  });
});

describe('textos', () => {
  it('descreve a antecedência e mascara o horário', () => {
    expect(descreverAntecedencia(0)).toBe('Avisa no horário');
    expect(descreverAntecedencia(15)).toBe('Avisa 15 min antes');
    expect(descreverAntecedencia(90)).toBe('Avisa 1 h 30 min antes');
    expect(mascararHorario('0710')).toBe('07:10');
    expect(mascararHorario('7')).toBe('7');
  });
});
