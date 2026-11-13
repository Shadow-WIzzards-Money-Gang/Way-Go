import { create, isAxiosError, type InternalAxiosRequestConfig } from 'axios';

import { env } from '@/config/env';
import { log } from '@/lib/log';
import { useSessao } from '@/store/sessao';

import { paraApiError } from './errors';

export const api = create({
  baseURL: env.apiBaseUrl,
  timeout: env.apiTimeoutMs,
  headers: { Accept: 'application/json' },
});

log.info('api', `URL base: ${env.apiBaseUrl} (timeout ${env.apiTimeoutMs} ms)`);

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

/** Momento em que cada requisição saiu, para logar a duração. */
const inicioDasRequisicoes = new WeakMap<object, number>();

function descrever(config: InternalAxiosRequestConfig | undefined): string {
  if (!config) return '(requisição desconhecida)';
  const metodo = (config.method ?? 'get').toUpperCase();
  const query = config.params ? ` ${JSON.stringify(config.params)}` : '';
  return `${metodo} ${config.url ?? ''}${query}`;
}

function duracao(config: InternalAxiosRequestConfig | undefined): string {
  const inicio = config ? inicioDasRequisicoes.get(config) : undefined;
  return inicio === undefined ? '' : ` (${Date.now() - inicio} ms)`;
}

api.interceptors.request.use((config) => {
  const token = useSessao.getState().sessao?.token;
  if (token) config.headers.set('Authorization', `Bearer ${token}`);
  inicioDasRequisicoes.set(config, Date.now());
  log.info('api', `→ Buscando ${descrever(config)}${token ? ' [com token]' : ''}`);
  return config;
});

api.interceptors.response.use(
  (response) => {
    log.info(
      'api',
      `← ${response.status} ${descrever(response.config)}${duracao(response.config)}`,
    );
    return response;
  },
  (erro: unknown) => {
    if (isAxiosError(erro)) {
      const config = erro.config;
      if (erro.response) {
        log.erro(
          'api',
          `✕ ${erro.response.status} ${descrever(config)}${duracao(config)}`,
          erro.response.data,
        );
      } else {
        // Sem resposta: servidor fora do ar, URL errada, sem internet ou timeout.
        log.erro(
          'api',
          `✕ Sem resposta em ${descrever(config)}${duracao(config)}: ${erro.code ?? ''} ${erro.message}` +
            ` — URL completa: ${config?.baseURL ?? ''}${config?.url ?? ''}`,
        );
      }
      if (erro.response?.status === 401 && !ehRotaDeAutenticacao(config?.url)) {
        log.info('sessao', 'Token recusado (401): encerrando sessão e abrindo o login');
        aoNaoAutorizado?.();
      }
    }
    return Promise.reject(paraApiError(erro));
  },
);
