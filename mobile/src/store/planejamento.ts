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
  definirEstacao: (campo, estacao) => set({ [campo]: estacao }),
  definirCriterio: (criterio) => set({ criterio }),
  inverter: () => set((s) => ({ origem: s.destino, destino: s.origem })),
  limpar: () => set({ origem: null, destino: null }),
}));
