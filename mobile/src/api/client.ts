import { create } from 'axios';

import { env } from '@/config/env';

import { paraApiError } from './errors';

export const api = create({
  baseURL: env.apiBaseUrl,
  timeout: env.apiTimeoutMs,
  headers: { Accept: 'application/json' },
});

api.interceptors.response.use(
  (response) => response,
  (erro: unknown) => Promise.reject(paraApiError(erro)),
);
