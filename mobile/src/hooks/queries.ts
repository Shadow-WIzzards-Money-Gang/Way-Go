import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  authApi,
  estacoesApi,
  linhasApi,
  redeApi,
  rotasApi,
  rotasSalvasApi,
  usuariosApi,
} from '@/api/waygo';
import { env } from '@/config/env';
import { cancelarTodosOsLembretes } from '@/services/notifications';
import { useRotasLocais } from '@/store/rotasLocais';
import { useLogado, useSessao } from '@/store/sessao';
import type {
  LoginRequest,
  RegistroRequest,
  RotaParams,
  RotaSalvaRequest,
  RotaSalvaResponse,
} from '@/types/api';

/** Chaves centralizadas: invalidar/prefetch sempre por aqui. */
export const queryKeys = {
  rede: ['rede'] as const,
  linhas: ['linhas'] as const,
  linha: (id: number) => ['linhas', id] as const,
  linhaStatus: (id: number) => ['linhas', id, 'status'] as const,
  estacoes: ['estacoes'] as const,
  estacao: (id: number) => ['estacoes', id] as const,
  rota: (params: RotaParams) => ['rotas', params] as const,
  /** Tudo que depende do usuário logado fica sob 'conta' e é descartado ao sair. */
  conta: ['conta'] as const,
  usuarioAtual: ['conta', 'eu'] as const,
  rotasSalvas: ['conta', 'rotas-salvas'] as const,
};

/** Rede completa (estações, linhas com status). Atualiza sozinha no intervalo do .env. */
export function useRede() {
  return useQuery({
    queryKey: queryKeys.rede,
    queryFn: redeApi.obter,
    refetchInterval: env.statusRefreshMs,
  });
}

export function useLinha(id: number) {
  return useQuery({
    queryKey: queryKeys.linha(id),
    queryFn: () => linhasApi.buscar(id),
    enabled: Number.isFinite(id),
    staleTime: 1000 * 60 * 30,
  });
}

export function useLinhaStatus(id: number) {
  return useQuery({
    queryKey: queryKeys.linhaStatus(id),
    queryFn: () => linhasApi.status(id),
    enabled: Number.isFinite(id),
    refetchInterval: env.statusRefreshMs,
  });
}

export function useEstacoes() {
  return useQuery({
    queryKey: queryKeys.estacoes,
    queryFn: estacoesApi.listar,
    staleTime: 1000 * 60 * 60,
  });
}

export function useEstacao(id: number) {
  return useQuery({
    queryKey: queryKeys.estacao(id),
    queryFn: () => estacoesApi.buscar(id),
    enabled: Number.isFinite(id),
    staleTime: 1000 * 60 * 30,
  });
}

/** Calcula a rota só quando origem e destino estão definidos. */
export function useRota(params: RotaParams | null) {
  return useQuery({
    queryKey: params ? queryKeys.rota(params) : ['rotas', 'vazia'],
    queryFn: () => rotasApi.calcular(params as RotaParams),
    enabled: params !== null,
    placeholderData: keepPreviousData,
    staleTime: 1000 * 60,
  });
}

// --- Conta ---

async function entrarCom(dados: LoginRequest) {
  const resposta = await authApi.login({ email: dados.email.trim(), senha: dados.senha });
  await useSessao.getState().entrar(resposta, dados.email);

  // Dados locais (linhas e lembretes) de outra conta não podem vazar para esta.
  const email = useSessao.getState().sessao?.email ?? null;
  const locais = useRotasLocais.getState();
  if (email && locais.dono !== email) {
    if (locais.dono !== null) {
      locais.limpar();
      await cancelarTodosOsLembretes();
    }
    useRotasLocais.getState().definirDono(email);
  }
  return resposta;
}

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: entrarCom,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.conta }),
  });
}

/** Cria a conta e já entra com as mesmas credenciais. */
export function useRegistro() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (dados: RegistroRequest) => {
      await authApi.registrar({ email: dados.email.trim(), senha: dados.senha });
      return entrarCom(dados);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.conta }),
  });
}

/** GET /usuarios/me: só roda com sessão. */
export function useUsuarioAtual() {
  const logado = useLogado();
  return useQuery({
    queryKey: queryKeys.usuarioAtual,
    queryFn: usuariosApi.eu,
    enabled: logado,
    staleTime: 1000 * 60 * 5,
  });
}

// --- Rotas salvas (exigem login) ---

export function useRotasSalvas() {
  const logado = useLogado();
  return useQuery({
    queryKey: queryKeys.rotasSalvas,
    queryFn: rotasSalvasApi.listar,
    enabled: logado,
  });
}

export function useSalvarRota() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: rotasSalvasApi.criar,
    onSuccess: (salva) => {
      queryClient.setQueryData<RotaSalvaResponse[]>(queryKeys.rotasSalvas, (atuais) =>
        atuais ? [salva, ...atuais] : atuais,
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.rotasSalvas });
    },
  });
}

export function useAtualizarRota() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: RotaSalvaRequest }) =>
      rotasSalvasApi.atualizar(id, dados),
    onSuccess: (salva) => {
      queryClient.setQueryData<RotaSalvaResponse[]>(queryKeys.rotasSalvas, (atuais) =>
        atuais?.map((r) => (r.id === salva.id ? salva : r)),
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.rotasSalvas });
    },
  });
}

export function useExcluirRota() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: rotasSalvasApi.excluir,
    onSuccess: (_, id) => {
      queryClient.setQueryData<RotaSalvaResponse[]>(queryKeys.rotasSalvas, (atuais) =>
        atuais?.filter((r) => r.id !== id),
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.rotasSalvas });
    },
  });
}
