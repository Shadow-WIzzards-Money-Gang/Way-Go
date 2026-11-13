import type {
  EstacaoDetalhe,
  EstacaoResumo,
  EstacoesParams,
  LinhaDetalhe,
  LinhaResumo,
  LinhaStatus,
  LoginRequest,
  LoginResponse,
  Pagina,
  Rede,
  RegistroRequest,
  RegistroResponse,
  Rota,
  RotaParams,
  RotaSalvaRequest,
  RotaSalvaResponse,
  UsuarioAtual,
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
  listar: ({ busca, ...params }: EstacoesParams) =>
    api
      .get<Pagina<EstacaoResumo>>('/estacoes', {
        params: { ...params, busca: busca?.trim() || undefined },
      })
      .then((r) => r.data),
  buscar: (id: number) => api.get<EstacaoDetalhe>(`/estacoes/${id}`).then((r) => r.data),
};

export const rotasApi = {
  calcular: (params: RotaParams) => api.get<Rota>('/rotas/calcular', { params }).then((r) => r.data),
};

export const authApi = {
  login: (dados: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', dados).then((r) => r.data),
  registrar: (dados: RegistroRequest) =>
    api.post<RegistroResponse>('/auth/registro', dados).then((r) => r.data),
};

/** Endpoints que exigem token (o interceptor de client.ts envia o Authorization). */
export const usuariosApi = {
  eu: () => api.get<UsuarioAtual>('/usuarios/me').then((r) => r.data),
};

export const rotasSalvasApi = {
  listar: () => api.get<RotaSalvaResponse[]>('/rotas').then((r) => r.data),
  buscar: (id: number) => api.get<RotaSalvaResponse>(`/rotas/${id}`).then((r) => r.data),
  criar: (dados: RotaSalvaRequest) =>
    api.post<RotaSalvaResponse>('/rotas', dados).then((r) => r.data),
  atualizar: (id: number, dados: RotaSalvaRequest) =>
    api.put<RotaSalvaResponse>(`/rotas/${id}`, dados).then((r) => r.data),
  excluir: (id: number) => api.delete<void>(`/rotas/${id}`).then(() => undefined),
};
