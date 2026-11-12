import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

import type { LinhaDaEstacao } from '@/types/api';

export interface Agendamento {
  /** 0 = domingo ... 6 = sábado. */
  dias: number[];
  /** "HH:mm" */
  horario: string;
}

/** O que o backend ainda não guarda sobre uma rota salva. */
export interface DadosLocaisRota {
  /** Linhas usadas pela rota, para mostrar badges e cruzar com o status de /rede. */
  linhas: LinhaDaEstacao[];
  /** Lembrete local (o backend ainda não tem endpoint de rota agendada). */
  agendamento: Agendamento | null;
}

const VAZIO: DadosLocaisRota = { linhas: [], agendamento: null };

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
 * Quando o backend expuser rotas agendadas, o agendamento sai daqui.
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
      version: 1,
      storage: createJSONStorage(() => AsyncStorage),
    },
  ),
);

export function dadosLocaisDa(porRota: Record<string, DadosLocaisRota>, rotaId: number) {
  return porRota[String(rotaId)] ?? VAZIO;
}
