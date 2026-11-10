import { keepPreviousData, useQuery } from '@tanstack/react-query';

import { estacoesApi, linhasApi, redeApi, rotasApi } from '@/api/waygo';
import { env } from '@/config/env';
import type { RotaParams } from '@/types/api';

/** Chaves centralizadas: invalidar/prefetch sempre por aqui. */
export const queryKeys = {
  rede: ['rede'] as const,
  linhas: ['linhas'] as const,
  linha: (id: number) => ['linhas', id] as const,
  linhaStatus: (id: number) => ['linhas', id, 'status'] as const,
  estacoes: ['estacoes'] as const,
  rota: (params: RotaParams) => ['rotas', params] as const,
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
