import AsyncStorage from '@react-native-async-storage/async-storage';
import { randomUUID } from 'expo-crypto';
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

import type { CriterioRota, LinhaDaEstacao } from '@/types/api';

import type { EstacaoSelecionada } from './planejamento';

export interface Agendamento {
  /** 0 = domingo ... 6 = sábado. */
  dias: number[];
  /** "HH:mm" */
  horario: string;
}

export interface RotaSalva {
  id: string;
  nome: string;
  origem: EstacaoSelecionada;
  destino: EstacaoSelecionada;
  criterio: CriterioRota;
  /** Linhas usadas pela rota quando foi salva, para mostrar badges e cruzar com o status. */
  linhas: LinhaDaEstacao[];
  agendamento: Agendamento | null;
  criadaEm: string;
}

export type NovaRotaSalva = Omit<RotaSalva, 'id' | 'criadaEm'>;

interface RotasSalvasState {
  rotas: RotaSalva[];
  alertasAtivos: boolean;
  adicionar: (rota: NovaRotaSalva) => RotaSalva;
  atualizar: (id: string, dados: Partial<NovaRotaSalva>) => void;
  remover: (id: string) => void;
  definirAlertas: (ativos: boolean) => void;
}

/**
 * Rotas salvas ficam só no aparelho: o backend ainda não tem endpoint para isso.
 * Quando existir, troque esta store por queries/mutations em src/api.
 */
export const useRotasSalvas = create<RotasSalvasState>()(
  persist(
    (set) => ({
      rotas: [],
      alertasAtivos: false,
      adicionar: (dados) => {
        const rota: RotaSalva = { ...dados, id: randomUUID(), criadaEm: new Date().toISOString() };
        set((s) => ({ rotas: [rota, ...s.rotas] }));
        return rota;
      },
      atualizar: (id, dados) =>
        set((s) => ({ rotas: s.rotas.map((r) => (r.id === id ? { ...r, ...dados } : r)) })),
      remover: (id) => set((s) => ({ rotas: s.rotas.filter((r) => r.id !== id) })),
      definirAlertas: (alertasAtivos) => set({ alertasAtivos }),
    }),
    {
      name: 'waygo:rotas-salvas',
      version: 1,
      storage: createJSONStorage(() => AsyncStorage),
    },
  ),
);
