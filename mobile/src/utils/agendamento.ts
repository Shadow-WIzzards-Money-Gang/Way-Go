import type { DiaSemana, RotaAgendada } from '@/types/api';

import { formatarDias, pluralizar } from './format';

/** Na mesma ordem de DIAS_DA_SEMANA (0 = domingo, como Date.getDay()). */
export const DIAS_API: readonly DiaSemana[] = [
  'DOMINGO',
  'SEGUNDA',
  'TERCA',
  'QUARTA',
  'QUINTA',
  'SEXTA',
  'SABADO',
];

export function diaParaIndice(dia: DiaSemana): number {
  return DIAS_API.indexOf(dia);
}

export function indicesParaDias(indices: readonly number[]): DiaSemana[] {
  return [...new Set(indices)]
    .sort((a, b) => a - b)
    .map((i) => DIAS_API[i])
    .filter((d): d is DiaSemana => d !== undefined);
}

/** "Seg a Sex · 07:10" */
export function descreverAgendamento(a: Pick<RotaAgendada, 'diasSemana' | 'horario'>): string {
  return `${formatarDias(a.diasSemana.map(diaParaIndice))} · ${a.horario}`;
}

/** "Avisa 10 min antes", "Avisa no horário", "Avisa 1 h antes". */
export function descreverAntecedencia(minutos: number): string {
  if (minutos === 0) return 'Avisa no horário';
  if (minutos < 60) return `Avisa ${minutos} min antes`;
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return `Avisa ${horas} h${resto ? ` ${resto} min` : ''} antes`;
}

/** Texto curto do card da rota a partir de todos os agendamentos dela. */
export function resumoAgendamentos(agendamentos: readonly RotaAgendada[]): string {
  if (agendamentos.length === 0) return 'Sem agendamento';
  const ativos = [...agendamentos]
    .filter((a) => a.ativa)
    .sort((a, b) => a.horarioPartidaMinutos - b.horarioPartidaMinutos);
  if (ativos.length === 0) {
    return agendamentos.length === 1 ? 'Agendamento pausado' : 'Agendamentos pausados';
  }
  const primeiro = descreverAgendamento(ativos[0]);
  if (ativos.length === 1) return primeiro;
  return `${primeiro} e mais ${pluralizar(ativos.length - 1, 'horário', 'horários')}`;
}

/**
 * Quando disparar o lembrete semanal: horário de partida menos a antecedência. Se passar da
 * meia-noite para trás, cai no dia anterior (ex.: segunda 00:10 com 30 min -> domingo 23:40).
 * `dia` usa 0 = domingo.
 */
export function momentoDoLembrete(
  dia: number,
  horarioMinutos: number,
  antecedenciaMinutos: number,
): { dia: number; hora: number; minuto: number } {
  const MINUTOS_SEMANA = 7 * 24 * 60;
  const total =
    (((dia * 24 * 60 + horarioMinutos - antecedenciaMinutos) % MINUTOS_SEMANA) + MINUTOS_SEMANA) %
    MINUTOS_SEMANA;
  return {
    dia: Math.floor(total / (24 * 60)),
    hora: Math.floor((total % (24 * 60)) / 60),
    minuto: total % 60,
  };
}

/** "0710" -> "07:10" enquanto o usuário digita. */
export function mascararHorario(texto: string): string {
  const digitos = texto.replace(/\D/g, '').slice(0, 4);
  return digitos.length <= 2 ? digitos : `${digitos.slice(0, 2)}:${digitos.slice(2)}`;
}
