import type { LinhaDaEstacao, Rota } from '@/types/api';

/** Linhas usadas por uma rota calculada, sem repetição e na ordem do trajeto. */
export function linhasDaRota(rota: Rota | undefined): LinhaDaEstacao[] {
  const unicas = new Map<number, LinhaDaEstacao>();
  for (const trecho of rota?.trechos ?? []) {
    const { id, nome, codigo, cor } = trecho.linha;
    if (!unicas.has(id)) unicas.set(id, { id, nome, codigo, cor });
  }
  return [...unicas.values()];
}
