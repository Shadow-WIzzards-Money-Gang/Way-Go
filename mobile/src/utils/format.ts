const DIAS_CURTOS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'] as const;

export const DIAS_DA_SEMANA = DIAS_CURTOS.map((rotulo, indice) => ({ indice, rotulo }));

/** "2026-11-10T13:48:12" -> "13:48". Retorna null se a data for inválida. */
export function formatarHora(iso: string | null | undefined): string | null {
  if (!iso) return null;
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return null;
  return `${String(data.getHours()).padStart(2, '0')}:${String(data.getMinutes()).padStart(2, '0')}`;
}

export function formatarMinutos(minutos: number): string {
  if (minutos < 60) return `${minutos} min`;
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
}

export function formatarDistancia(metros: number): string {
  return metros < 1000 ? `${metros} m` : `${(metros / 1000).toFixed(1).replace('.', ',')} km`;
}

export function pluralizar(qtd: number, singular: string, plural: string): string {
  return `${qtd} ${qtd === 1 ? singular : plural}`;
}

/**
 * Resume os dias selecionados como nos protótipos: [1..5] -> "Seg a Sex", [2,4] -> "Ter e Qui".
 * Dias usam 0 = domingo, como Date.getDay().
 */
export function formatarDias(dias: readonly number[]): string {
  const ordenados = [...new Set(dias)].sort((a, b) => a - b);
  if (ordenados.length === 0) return '';
  if (ordenados.length === 7) return 'Todos os dias';

  const consecutivos = ordenados.every((dia, i) => i === 0 || dia === ordenados[i - 1] + 1);
  if (consecutivos && ordenados.length >= 3) {
    return `${DIAS_CURTOS[ordenados[0]]} a ${DIAS_CURTOS[ordenados[ordenados.length - 1]]}`;
  }

  const rotulos = ordenados.map((dia) => DIAS_CURTOS[dia]);
  if (rotulos.length === 1) return rotulos[0];
  return `${rotulos.slice(0, -1).join(', ')} e ${rotulos[rotulos.length - 1]}`;
}

const MESES = [
  'janeiro',
  'fevereiro',
  'março',
  'abril',
  'maio',
  'junho',
  'julho',
  'agosto',
  'setembro',
  'outubro',
  'novembro',
  'dezembro',
] as const;

/** "2026-11-12T09:30:00" -> "12 de novembro de 2026". Retorna null se a data for inválida. */
export function formatarData(iso: string | null | undefined): string | null {
  if (!iso) return null;
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return null;
  return `${data.getDate()} de ${MESES[data.getMonth()]} de ${data.getFullYear()}`;
}

/** Valida "HH:mm" (24h). */
export function ehHorarioValido(valor: string): boolean {
  return /^([01]\d|2[0-3]):[0-5]\d$/.test(valor);
}
