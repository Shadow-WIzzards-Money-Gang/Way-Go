import { isAxiosError } from 'axios';

import type { ErroResponse } from '@/types/api';

export type TipoErroApi =
  | 'rede'
  | 'timeout'
  | 'nao-autorizado'
  | 'nao-encontrado'
  | 'conflito'
  | 'sem-rota'
  | 'invalido'
  | 'servidor';

/** Erro normalizado da API: telas só lidam com este tipo, nunca com AxiosError. */
export class ApiError extends Error {
  readonly tipo: TipoErroApi;
  readonly status?: number;

  constructor(message: string, tipo: TipoErroApi, status?: number) {
    super(message);
    this.name = 'ApiError';
    this.tipo = tipo;
    this.status = status;
  }
}

function tipoPorStatus(status: number): TipoErroApi {
  if (status === 401) return 'nao-autorizado';
  if (status === 404) return 'nao-encontrado';
  if (status === 409) return 'conflito';
  if (status === 422) return 'sem-rota';
  if (status >= 400 && status < 500) return 'invalido';
  return 'servidor';
}

function ehErroResponse(valor: unknown): valor is ErroResponse {
  return typeof valor === 'object' && valor !== null && 'message' in valor;
}

export function paraApiError(erro: unknown): ApiError {
  if (erro instanceof ApiError) return erro;

  if (isAxiosError(erro)) {
    if (erro.code === 'ECONNABORTED' || erro.code === 'ETIMEDOUT') {
      return new ApiError('O servidor demorou para responder. Tente novamente.', 'timeout');
    }
    if (!erro.response) {
      return new ApiError('Não foi possível conectar ao servidor. Verifique sua conexão.', 'rede');
    }
    const { status, data } = erro.response;
    const padrao =
      status === 401 ? 'Você precisa entrar na sua conta para continuar.' : 'Algo deu errado no servidor.';
    const mensagem = ehErroResponse(data) && data.message ? data.message : padrao;
    return new ApiError(mensagem, tipoPorStatus(status), status);
  }

  return new ApiError('Erro inesperado. Tente novamente.', 'servidor');
}
