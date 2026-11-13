import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

import type { LinhaDaEstacao } from '@/types/api';

/** O que o backend ainda não guarda sobre uma rota salva. */
export interface DadosLocaisRota {
  /** Linhas usadas pela rota, para mostrar badges e cruzar com o status de /rede. */
  linhas: LinhaDaEstacao[];
}

const VAZIO: DadosLocaisRota = { linhas: [] };

interface RotasLocaisState {
  /** E-mail da conta a que estes dados pertencem. */
  dono: string | null;
  /** Indexado pelo id da rota no servidor (as chaves viram string no JSON). */
  porRota: Record<string, DadosLocaisRota>;
  alertasAtivos: boolean;
  definirDono: (email: string) => void;
  definir: (rotaId: number, dados: Partial<DadosLocaisRota>) => void;
  remover: (rotaId: number) => void;
  definirAlertas: (ativos: boolean) => void;
  limpar: () => void;
}

/**
 * Complementa as rotas salvas da conta (que vêm da API) com dados que só existem no aparelho.
 * Os agendamentos vêm do backend (/rotas/agendamentos); aqui ficam só as linhas de cada rota.
 */
export const useRotasLocais = create<RotasLocaisState>()(
  persist(
    (set) => ({
      dono: null,
      porRota: {},
      alertasAtivos: false,
      definirDono: (dono) => set({ dono }),
      definir: (rotaId, dados) =>
        set((s) => ({
          porRota: {
            ...s.porRota,
            [String(rotaId)]: { ...VAZIO, ...s.porRota[String(rotaId)], ...dados },
          },
        })),
      remover: (rotaId) =>
        set((s) => {
          const { [String(rotaId)]: _removida, ...resto } = s.porRota;
          return { porRota: resto };
        }),
      definirAlertas: (alertasAtivos) => set({ alertasAtivos }),
      limpar: () => set({ dono: null, porRota: {}, alertasAtivos: false }),
    }),
    {
      name: 'waygo:rotas-locais',
      // v2: o agendamento saiu do aparelho e passou a vir do backend.
      version: 2,
      storage: createJSONStorage(() => AsyncStorage),
      migrate: (salvo) => {
        const estado = (salvo ?? {}) as Partial<RotasLocaisState>;
        const porRota: Record<string, DadosLocaisRota> = {};
        for (const [id, dados] of Object.entries(estado.porRota ?? {})) {
          porRota[id] = { linhas: dados?.linhas ?? [] };
        }
        return { ...estado, porRota } as RotasLocaisState;
      },
    },
  ),
);

export function dadosLocaisDa(porRota: Record<string, DadosLocaisRota>, rotaId: number) {
  return porRota[String(rotaId)] ?? VAZIO;
}
