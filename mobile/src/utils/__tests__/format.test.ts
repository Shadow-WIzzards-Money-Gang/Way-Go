import { describe, expect, it } from '@jest/globals';
import { ehHorarioValido, formatarDias, formatarHora, formatarMinutos } from '../format';

describe('formatarDias', () => {
  it('resume dias consecutivos como intervalo', () => {
    expect(formatarDias([1, 2, 3, 4, 5])).toBe('Seg a Sex');
  });

  it('lista dias soltos com "e"', () => {
    expect(formatarDias([4, 2])).toBe('Ter e Qui');
    expect(formatarDias([0, 3, 6])).toBe('Dom, Qua e Sáb');
  });

  it('trata todos os dias e lista vazia', () => {
    expect(formatarDias([0, 1, 2, 3, 4, 5, 6])).toBe('Todos os dias');
    expect(formatarDias([])).toBe('');
  });
});

describe('formatarHora', () => {
  it('extrai HH:mm de um LocalDateTime do backend', () => {
    expect(formatarHora('2026-11-10T13:48:12')).toBe('13:48');
  });

  it('retorna null para valores inválidos', () => {
    expect(formatarHora(null)).toBeNull();
    expect(formatarHora('não é data')).toBeNull();
  });
});

describe('formatarMinutos', () => {
  it('usa horas acima de 60 minutos', () => {
    expect(formatarMinutos(73)).toBe('1 h 13 min');
    expect(formatarMinutos(45)).toBe('45 min');
    expect(formatarMinutos(120)).toBe('2 h');
  });
});

describe('ehHorarioValido', () => {
  it('aceita apenas HH:mm em 24h', () => {
    expect(ehHorarioValido('07:10')).toBe(true);
    expect(ehHorarioValido('23:59')).toBe(true);
    expect(ehHorarioValido('24:00')).toBe(false);
    expect(ehHorarioValido('7:10')).toBe(false);
  });
});
