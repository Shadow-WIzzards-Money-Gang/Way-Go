import type {
  EstacaoDetalhe,
  EstacaoResumo,
  LinhaDetalhe,
  LinhaResumo,
  LinhaStatus,
  Rede,
  Rota,
  RotaParams,
} from '@/types/api';

import { api } from './client';

/** Funções tipadas para cada endpoint do backend. Nenhuma tela chama `api` direto. */

export const redeApi = {
  obter: () => api.get<Rede>('/rede').then((r) => r.data),
};

export const linhasApi = {
  listar: () => api.get<LinhaResumo[]>('/linhas').then((r) => r.data),
  buscar: (id: number) => api.get<LinhaDetalhe>(`/linhas/${id}`).then((r) => r.data),
  status: (id: number) => api.get<LinhaStatus>(`/linhas/${id}/status`).then((r) => r.data),
};

export const estacoesApi = {
  listar: () => api.get<EstacaoResumo[]>('/estacoes').then((r) => r.data),
  buscar: (id: number) => api.get<EstacaoDetalhe>(`/estacoes/${id}`).then((r) => r.data),
};

export const rotasApi = {
  calcular: (params: RotaParams) => api.get<Rota>('/rotas', { params }).then((r) => r.data),
};
