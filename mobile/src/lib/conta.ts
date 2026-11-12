import AsyncStorage from '@react-native-async-storage/async-storage';
import { type Href, router } from 'expo-router';

import { queryKeys } from '@/hooks/queries';
import { queryClient } from '@/lib/queryClient';
import { cancelarTodosOsLembretes } from '@/services/notifications';
import { useRotasLocais } from '@/store/rotasLocais';
import { useSessao } from '@/store/sessao';

/** Por que o login foi aberto: muda o texto de apoio da tela. */
export type MotivoLogin = 'sessao-expirada' | 'restrito' | 'rotas' | 'salvar-rota';

export interface ParametrosLogin {
  motivo?: MotivoLogin;
  /** Href para onde ir depois de entrar (ex.: o formulário de salvar rota). */
  proximo?: string;
}

let loginAberto = false;

/** A tela de login avisa quando está montada, para um 401 não empilhar dois logins. */
export function marcarLoginAberto(aberto: boolean): void {
  loginAberto = aberto;
}

function semVazios(params: ParametrosLogin): Record<string, string> {
  return Object.fromEntries(
    Object.entries(params).filter((par): par is [string, string] => typeof par[1] === 'string'),
  );
}

export function abrirLogin(params: ParametrosLogin = {}): void {
  if (loginAberto) return;
  loginAberto = true;
  router.push({ pathname: '/login', params: semVazios(params) });
}

/** `aPartirDoLogin`: o cadastro fica por cima do login (voltar leva a ele; ao concluir, fecha os dois). */
export function abrirCadastro(params: ParametrosLogin = {}, aPartirDoLogin = false): void {
  router.push({
    pathname: '/cadastro',
    params: { ...semVazios(params), ...(aPartirDoLogin ? { deLogin: '1' } : {}) },
  });
}

/** Depois de entrar: segue para `proximo` ou só fecha as telas de conta. */
export function concluirEntrada(proximo: string | undefined, telasAbertas: 1 | 2): void {
  if (router.canDismiss()) router.dismiss(telasAbertas);
  else if (router.canGoBack()) router.back();
  if (proximo) router.push(proximo as Href);
}

/** "/rota-salva" + {a: '1'} -> "/rota-salva?a=1" (URLSearchParams é incompleto no React Native). */
export function hrefComParams(pathname: string, params: Record<string, string | undefined>): string {
  const query = Object.entries(params)
    .filter((par): par is [string, string] => typeof par[1] === 'string')
    .map(([chave, valor]) => `${encodeURIComponent(chave)}=${encodeURIComponent(valor)}`)
    .join('&');
  return query ? `${pathname}?${query}` : pathname;
}

/**
 * Token expirado/recusado: descarta o token e o cache da conta, mas mantém os lembretes
 * do aparelho, que voltam a valer quando a mesma pessoa entrar de novo.
 */
export async function descartarSessaoExpirada(): Promise<void> {
  await useSessao.getState().sair();
  queryClient.removeQueries({ queryKey: queryKeys.conta });
}

/** Saída pedida pelo usuário: também apaga os dados locais das rotas e os lembretes. */
export async function encerrarSessao(): Promise<void> {
  await descartarSessaoExpirada();
  await limparDadosLocaisDaConta();
}

const CHAVE_ROTAS_ANTIGAS = 'waygo:rotas-salvas';

/** Rotas da versão que salvava só no aparelho: são descartadas (agora as rotas ficam na conta). */
export async function descartarRotasAntigasDoAparelho(): Promise<void> {
  try {
    if (await AsyncStorage.getItem(CHAVE_ROTAS_ANTIGAS)) {
      await AsyncStorage.removeItem(CHAVE_ROTAS_ANTIGAS);
      await cancelarTodosOsLembretes();
    }
  } catch {
    // Sem acesso ao armazenamento: tenta de novo na próxima abertura.
  }
}

export async function limparDadosLocaisDaConta(): Promise<void> {
  useRotasLocais.getState().limpar();
  await cancelarTodosOsLembretes();
}
