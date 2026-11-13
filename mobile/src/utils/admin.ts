import { ApiError } from '@/api/errors';
import type { StatusOperacional, TipoTransporte } from '@/types/api';

export const TIPOS_TRANSPORTE: { valor: TipoTransporte; rotulo: string }[] = [
  { valor: 'METRO', rotulo: 'Metrô' },
  { valor: 'TREM_METROPOLITANO', rotulo: 'Trem' },
  { valor: 'MONOTRILHO', rotulo: 'Monotrilho' },
];

/** Status que o administrador pode definir (DESCONHECIDO é só da API). */
export const STATUS_MANUAIS: StatusOperacional[] = [
  'NORMAL',
  'VELOCIDADE_REDUZIDA',
  'OPERACAO_PARCIAL',
  'ATIVIDADE_PROGRAMADA',
  'OPERACAO_TRANSITORIA',
  'PARALISADA',
  'OPERACAO_ENCERRADA',
];

/** Mensagem para o formulário; null quando o 401 já abriu o login. */
export function mensagemDeErro(erro: unknown, padrao: string): string | null {
  if (erro instanceof ApiError) return erro.tipo === 'nao-autorizado' ? null : erro.message;
  return padrao;
}

/** "#0455a1" ou "0455A1" -> "#0455A1"; null se não for uma cor #RRGGBB. Vazio vira "". */
export function normalizarCor(texto: string): string | null {
  const limpo = texto.trim().replace(/^#/, '');
  if (limpo === '') return '';
  return /^[0-9a-fA-F]{6}$/.test(limpo) ? `#${limpo.toUpperCase()}` : null;
}

/** Aceita vírgula ou ponto decimal ("-23,55"). null se não for um número dentro do limite. */
export function lerCoordenada(texto: string, limite: 90 | 180): number | null {
  const normalizado = texto.trim().replace(',', '.');
  if (!/^-?\d+(\.\d+)?$/.test(normalizado)) return null;
  const valor = Number(normalizado);
  return Math.abs(valor) <= limite ? valor : null;
}

/** Até 7 casas, como o banco (NUMBER(10,7)). */
export function formatarCoordenada(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? '' : String(Number(valor.toFixed(7)));
}

/** Move o item de `de` para `para`, devolvendo uma lista nova. */
export function mover<T>(lista: readonly T[], de: number, para: number): T[] {
  if (para < 0 || para >= lista.length || de === para) return [...lista];
  const copia = [...lista];
  const [item] = copia.splice(de, 1);
  copia.splice(para, 0, item);
  return copia;
}
