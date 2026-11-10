import { useMemo } from 'react';

import type { EstacaoRede, LinhaRede, Rede } from '@/types/api';

export interface RedeIndex {
  linhaPorId: Map<number, LinhaRede>;
  estacaoPorId: Map<number, EstacaoRede>;
}

/** Índices por id sobre a resposta de /rede, para buscas O(1) nas telas. */
export function useRedeIndex(rede: Rede | undefined): RedeIndex {
  return useMemo(
    () => ({
      linhaPorId: new Map((rede?.linhas ?? []).map((l) => [l.id, l])),
      estacaoPorId: new Map((rede?.estacoes ?? []).map((e) => [e.id, e])),
    }),
    [rede],
  );
}

/** "Jundiaí ↔ Palmeiras-Barra Funda": primeira e última estação da linha. */
export function extremosDaLinha(linha: LinhaRede, index: RedeIndex): string | null {
  const primeira = index.estacaoPorId.get(linha.estacaoIds[0]);
  const ultima = index.estacaoPorId.get(linha.estacaoIds[linha.estacaoIds.length - 1]);
  if (!primeira || !ultima) return null;
  return `${primeira.nome} ↔ ${ultima.nome}`;
}
