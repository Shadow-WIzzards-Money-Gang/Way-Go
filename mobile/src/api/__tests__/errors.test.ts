import { describe, expect, it } from '@jest/globals';
import { AxiosError, AxiosHeaders } from 'axios';

import { ApiError, paraApiError } from '../errors';

function erroHttp(status: number, data: unknown) {
  return new AxiosError('falha', 'ERR_BAD_RESPONSE', undefined, undefined, {
    status,
    statusText: '',
    data,
    headers: {},
    config: { headers: new AxiosHeaders() },
  });
}

describe('paraApiError', () => {
  it('usa a mensagem do ErroResponse do backend', () => {
    const erro = paraApiError(
      erroHttp(422, { status: 422, message: 'Nenhuma rota entre Sé e Luz', data: '' }),
    );
    expect(erro).toBeInstanceOf(ApiError);
    expect(erro.tipo).toBe('sem-rota');
    expect(erro.message).toBe('Nenhuma rota entre Sé e Luz');
  });

  it('classifica 404 e 5xx', () => {
    expect(paraApiError(erroHttp(404, {})).tipo).toBe('nao-encontrado');
    expect(paraApiError(erroHttp(500, 'html')).tipo).toBe('servidor');
  });

  it('identifica falta de conexão e timeout', () => {
    expect(paraApiError(new AxiosError('Network Error', 'ERR_NETWORK')).tipo).toBe('rede');
    expect(paraApiError(new AxiosError('timeout', 'ECONNABORTED')).tipo).toBe('timeout');
  });
});
