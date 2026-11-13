import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  adminApi,
  agendamentosApi,
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
  EstacaoRequest,
  EstacoesParams,
  LinhaAtualizacaoRequest,
  LinhaRequest,
  LoginRequest,
  RegistroRequest,
  RotaAgendada,
  RotaAgendadaRequest,
  RotaParams,
  RotaSalvaRequest,
  RotaSalvaResponse,
  StatusLinhaRequest,
} from '@/types/api';

/** Chaves centralizadas: invalidar/prefetch sempre por aqui. */
export const queryKeys = {
  rede: ['rede'] as const,
  linhas: ['linhas'] as const,
  /** /linhas: inclui as inativas (a /rede só traz as que estão no grafo). */
  todasAsLinhas: ['linhas', 'todas'] as const,
  linha: (id: number) => ['linhas', id] as const,
  linhaStatus: (id: number) => ['linhas', id, 'status'] as const,
  estacoes: (params: EstacoesParams) => ['estacoes', 'pagina', params] as const,
  estacao: (id: number) => ['estacoes', id] as const,
  rota: (params: RotaParams) => ['rotas', params] as const,
  /** Tudo que depende do usuário logado fica sob 'conta' e é descartado ao sair. */
  conta: ['conta'] as const,
  usuarioAtual: ['conta', 'eu'] as const,
  rotasSalvas: ['conta', 'rotas-salvas'] as const,
  agendamentos: ['conta', 'agendamentos'] as const,
  usuarios: (pagina: number) => ['conta', 'usuarios', pagina] as const,
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

/** Uma página de /estacoes. Mantém a página anterior na tela enquanto a próxima carrega. */
export function useEstacoes(params: EstacoesParams) {
  return useQuery({
    queryKey: queryKeys.estacoes(params),
    queryFn: () => estacoesApi.listar(params),
    placeholderData: keepPreviousData,
    staleTime: 1000 * 60 * 10,
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
      // O banco apaga os agendamentos da rota em cascata.
      queryClient.setQueryData<RotaAgendada[]>(queryKeys.agendamentos, (atuais) =>
        atuais?.filter((a) => a.idRotaSalva !== id),
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.rotasSalvas });
      void queryClient.invalidateQueries({ queryKey: queryKeys.agendamentos });
    },
  });
}

// --- Agendamentos das rotas salvas (exigem login; o backend só devolve os da conta) ---

export function useAgendamentos() {
  const logado = useLogado();
  return useQuery({
    queryKey: queryKeys.agendamentos,
    queryFn: agendamentosApi.listar,
    enabled: logado,
  });
}

/** Aplica o agendamento devolvido pela API no cache, sem esperar o refetch. */
function substituirNoCache(atuais: RotaAgendada[] | undefined, novo: RotaAgendada) {
  if (!atuais) return atuais;
  const existe = atuais.some((a) => a.id === novo.id);
  const lista = existe ? atuais.map((a) => (a.id === novo.id ? novo : a)) : [...atuais, novo];
  return lista.sort((a, b) => a.horarioPartidaMinutos - b.horarioPartidaMinutos);
}

export function useCriarAgendamento() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ idRota, dados }: { idRota: number; dados: RotaAgendadaRequest }) =>
      agendamentosApi.criar(idRota, dados),
    onSuccess: (novo) => {
      queryClient.setQueryData<RotaAgendada[]>(queryKeys.agendamentos, (atuais) =>
        substituirNoCache(atuais, novo),
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.agendamentos });
    },
  });
}

export function useAtualizarAgendamento() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: RotaAgendadaRequest }) =>
      agendamentosApi.atualizar(id, dados),
    onSuccess: (atualizado) => {
      queryClient.setQueryData<RotaAgendada[]>(queryKeys.agendamentos, (atuais) =>
        substituirNoCache(atuais, atualizado),
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.agendamentos });
    },
  });
}

export function useExcluirAgendamento() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: agendamentosApi.excluir,
    onSuccess: (_, id) => {
      queryClient.setQueryData<RotaAgendada[]>(queryKeys.agendamentos, (atuais) =>
        atuais?.filter((a) => a.id !== id),
      );
      void queryClient.invalidateQueries({ queryKey: queryKeys.agendamentos });
    },
  });
}

// --- Administração ---

/** true só quando /usuarios/me confirma a role ADMINISTRADOR (o backend valida de novo). */
export function useEhAdmin(): boolean {
  const { data } = useUsuarioAtual();
  return data?.roles.includes('ADMINISTRADOR') ?? false;
}

export function useUsuarios(pagina: number) {
  const ehAdmin = useEhAdmin();
  return useQuery({
    queryKey: queryKeys.usuarios(pagina),
    queryFn: () => usuariosApi.listar(pagina),
    enabled: ehAdmin,
    placeholderData: keepPreviousData,
  });
}

/** Todas as linhas, inclusive as desativadas. */
export function useTodasAsLinhas() {
  return useQuery({ queryKey: queryKeys.todasAsLinhas, queryFn: linhasApi.listar });
}

/** Qualquer mudança na rede afeta listas, detalhes, o mapa e as rotas já calculadas. */
function useInvalidarRede() {
  const queryClient = useQueryClient();
  return () =>
    Promise.all(
      [queryKeys.linhas, ['estacoes'], queryKeys.rede, ['rotas']].map((queryKey) =>
        queryClient.invalidateQueries({ queryKey }),
      ),
    );
}

export function useCriarLinha() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: (dados: LinhaRequest) => adminApi.criarLinha(dados),
    onSuccess: invalidar,
  });
}

export function useAtualizarLinha() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: LinhaAtualizacaoRequest }) =>
      adminApi.atualizarLinha(id, dados),
    onSuccess: invalidar,
  });
}

export function useDesativarLinha() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: (id: number) => adminApi.desativarLinha(id),
    onSuccess: invalidar,
  });
}

export function useReordenarEstacoes() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: ({ id, idsEstacoes }: { id: number; idsEstacoes: number[] }) =>
      adminApi.reordenarEstacoes(id, idsEstacoes),
    onSuccess: invalidar,
  });
}

export function useAlterarStatusLinha() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: StatusLinhaRequest }) =>
      adminApi.alterarStatus(id, dados),
    onSuccess: invalidar,
  });
}

export function useCriarEstacao() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: (dados: EstacaoRequest) => adminApi.criarEstacao(dados),
    onSuccess: invalidar,
  });
}

export function useAtualizarEstacao() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: EstacaoRequest }) =>
      adminApi.atualizarEstacao(id, dados),
    onSuccess: invalidar,
  });
}

export function useDesativarEstacao() {
  const invalidar = useInvalidarRede();
  return useMutation({
    mutationFn: (id: number) => adminApi.desativarEstacao(id),
    onSuccess: invalidar,
  });
}
