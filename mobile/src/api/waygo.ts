import type {
  EstacaoDetalhe,
  EstacaoRequest,
  EstacaoResumo,
  EstacoesParams,
  EstacaoDaLinha,
  LinhaAtualizacaoRequest,
  LinhaDetalhe,
  LinhaRequest,
  LinhaResumo,
  LinhaStatus,
  LoginRequest,
  LoginResponse,
  Pagina,
  Rede,
  RegistroRequest,
  RegistroResponse,
  Rota,
  RotaAgendada,
  RotaAgendadaRequest,
  RotaParams,
  RotaSalvaRequest,
  RotaSalvaResponse,
  StatusLinhaRequest,
  Usuario,
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
  calcular: ({ alternativa, ...params }: RotaParams) =>
    api
      .get<Rota>('/rotas/calcular', {
        params: alternativa ? { ...params, alternativa: true } : params,
      })
      .then((r) => r.data),
};

export const authApi = {
  login: (dados: LoginRequest) => api.post<LoginResponse>('/auth/login', dados).then((r) => r.data),
  registrar: (dados: RegistroRequest) =>
    api.post<RegistroResponse>('/auth/registro', dados).then((r) => r.data),
};

/** Endpoints que exigem token (o interceptor de client.ts envia o Authorization). */
export const usuariosApi = {
  eu: () => api.get<UsuarioAtual>('/usuarios/me').then((r) => r.data),
  /** Só administradores. */
  listar: (pagina: number, tamanho = 20) =>
    api.get<Pagina<Usuario>>('/usuarios', { params: { pagina, tamanho } }).then((r) => r.data),
};

/** Escrita na rede (linhas e estações): só administradores. */
export const adminApi = {
  criarLinha: (dados: LinhaRequest) => api.post<LinhaResumo>('/linhas', dados).then((r) => r.data),
  atualizarLinha: (id: number, dados: LinhaAtualizacaoRequest) =>
    api.put<LinhaResumo>(`/linhas/${id}`, dados).then((r) => r.data),
  desativarLinha: (id: number) => api.delete(`/linhas/${id}`).then(() => undefined),
  reordenarEstacoes: (id: number, idsEstacoes: number[]) =>
    api.put<EstacaoDaLinha[]>(`/linhas/${id}/estacoes/ordem`, { idsEstacoes }).then((r) => r.data),
  alterarStatus: (id: number, dados: StatusLinhaRequest) =>
    api.patch<LinhaStatus>(`/linhas/${id}/status`, dados).then((r) => r.data),
  criarEstacao: (dados: EstacaoRequest) =>
    api.post<EstacaoResumo>('/estacoes', dados).then((r) => r.data),
  atualizarEstacao: (id: number, dados: EstacaoRequest) =>
    api.put<EstacaoResumo>(`/estacoes/${id}`, dados).then((r) => r.data),
  desativarEstacao: (id: number) => api.delete(`/estacoes/${id}`).then(() => undefined),
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

/** Agendamentos das rotas salvas do usuário logado (o backend só devolve os da conta). */
export const agendamentosApi = {
  listar: () => api.get<RotaAgendada[]>('/rotas/agendamentos').then((r) => r.data),
  listarDaRota: (idRota: number) =>
    api.get<RotaAgendada[]>(`/rotas/${idRota}/agendamentos`).then((r) => r.data),
  criar: (idRota: number, dados: RotaAgendadaRequest) =>
    api.post<RotaAgendada>(`/rotas/${idRota}/agendamentos`, dados).then((r) => r.data),
  atualizar: (id: number, dados: RotaAgendadaRequest) =>
    api.put<RotaAgendada>(`/rotas/agendamentos/${id}`, dados).then((r) => r.data),
  excluir: (id: number) => api.delete<void>(`/rotas/agendamentos/${id}`).then(() => undefined),
};
