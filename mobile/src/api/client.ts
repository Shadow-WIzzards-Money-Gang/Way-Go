import { create, isAxiosError } from 'axios';

import { env } from '@/config/env';
import { useSessao } from '@/store/sessao';

import { paraApiError } from './errors';

export const api = create({
  baseURL: env.apiBaseUrl,
  timeout: env.apiTimeoutMs,
  headers: { Accept: 'application/json' },
});

type AoNaoAutorizado = () => void;
let aoNaoAutorizado: AoNaoAutorizado | null = null;

/**
 * Registra quem reage a um 401 (o layout raiz, que abre o login). Fica fora desta camada
 * para que src/api não dependa do router. Retorna a função que remove o registro.
 */
export function definirAoNaoAutorizado(fn: AoNaoAutorizado): () => void {
  aoNaoAutorizado = fn;
  return () => {
    if (aoNaoAutorizado === fn) aoNaoAutorizado = null;
  };
}

/** 401 em /auth/* é "e-mail ou senha inválidos": a própria tela de login trata. */
const ehRotaDeAutenticacao = (url: string | undefined) => Boolean(url?.startsWith('/auth/'));

api.interceptors.request.use((config) => {
  const token = useSessao.getState().sessao?.token;
  if (token) config.headers.set('Authorization', `Bearer ${token}`);
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (erro: unknown) => {
    if (
      isAxiosError(erro) &&
      erro.response?.status === 401 &&
      !ehRotaDeAutenticacao(erro.config?.url)
    ) {
      aoNaoAutorizado?.();
    }
    return Promise.reject(paraApiError(erro));
  },
);
