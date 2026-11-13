import { create } from 'zustand';

import type { CriterioRota } from '@/types/api';

export interface EstacaoSelecionada {
  id: number;
  nome: string;
}

export type CampoEstacao = 'origem' | 'destino';

interface PlanejamentoState {
  origem: EstacaoSelecionada | null;
  destino: EstacaoSelecionada | null;
  criterio: CriterioRota;
  definirEstacao: (campo: CampoEstacao, estacao: EstacaoSelecionada) => void;
  definirCriterio: (criterio: CriterioRota) => void;
  inverter: () => void;
  limpar: () => void;
}

/** Origem/destino escolhidos na tela do Mapa (compartilhado com o modal de seleção). */
export const usePlanejamento = create<PlanejamentoState>((set) => ({
  origem: null,
  destino: null,
  criterio: 'EQUILIBRADO',
  // Origem e destino iguais não formam rota: escolher no outro campo a mesma estação o esvazia.
  definirEstacao: (campo, estacao) =>
    set((s) => {
      const outro: CampoEstacao = campo === 'origem' ? 'destino' : 'origem';
      return s[outro]?.id === estacao.id
        ? { [campo]: estacao, [outro]: null }
        : { [campo]: estacao };
    }),
  definirCriterio: (criterio) => set({ criterio }),
  inverter: () => set((s) => ({ origem: s.destino, destino: s.origem })),
  limpar: () => set({ origem: null, destino: null }),
}));
