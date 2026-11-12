import { create } from 'zustand';

import { armazenamentoSeguro } from '@/lib/armazenamentoSeguro';
import type { LoginResponse } from '@/types/api';

const CHAVE = 'waygo.sessao';

export interface Sessao {
  token: string;
  email: string;
  /** ISO-8601 em UTC. */
  expiraEm: string;
}

interface SessaoState {
  sessao: Sessao | null;
  /** true depois de ler o armazenamento seguro (evita piscar a tela de "entre na conta"). */
  carregada: boolean;
  carregar: () => Promise<void>;
  entrar: (resposta: LoginResponse, email: string) => Promise<void>;
  sair: () => Promise<void>;
}

function ehSessao(valor: unknown): valor is Sessao {
  if (typeof valor !== 'object' || valor === null) return false;
  const v = valor as Record<string, unknown>;
  return typeof v.token === 'string' && typeof v.email === 'string' && typeof v.expiraEm === 'string';
}

export function sessaoExpirada(sessao: Sessao, agora = Date.now()): boolean {
  const expira = Date.parse(sessao.expiraEm);
  return Number.isNaN(expira) || expira <= agora;
}

/**
 * Sessão do usuário (token JWT + e-mail). O token fica no armazenamento seguro do aparelho
 * e é lido pelo interceptor de src/api/client.ts; nenhuma tela manipula o token direto.
 */
export const useSessao = create<SessaoState>((set) => ({
  sessao: null,
  carregada: false,
  carregar: async () => {
    try {
      const bruto = await armazenamentoSeguro.ler(CHAVE);
      const lida: unknown = bruto ? JSON.parse(bruto) : null;
      if (ehSessao(lida) && !sessaoExpirada(lida)) {
        set({ sessao: lida, carregada: true });
        return;
      }
      if (bruto) await armazenamentoSeguro.apagar(CHAVE);
    } catch {
      // Armazenamento ilegível: segue sem sessão.
    }
    set({ sessao: null, carregada: true });
  },
  entrar: async (resposta, email) => {
    const sessao: Sessao = {
      token: resposta.token,
      email: email.trim().toLowerCase(),
      expiraEm: resposta.expiraEm,
    };
    set({ sessao, carregada: true });
    await armazenamentoSeguro.gravar(CHAVE, JSON.stringify(sessao)).catch(() => undefined);
  },
  sair: async () => {
    set({ sessao: null });
    await armazenamentoSeguro.apagar(CHAVE).catch(() => undefined);
  },
}));

export const useLogado = () => useSessao((s) => s.sessao !== null);
export const useEmailDaSessao = () => useSessao((s) => s.sessao?.email ?? null);
