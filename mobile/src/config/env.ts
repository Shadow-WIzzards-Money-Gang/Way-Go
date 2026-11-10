import Constants from 'expo-constants';
import { Platform } from 'react-native';
import { z } from 'zod';

/**
 * Único ponto de leitura de variáveis de ambiente do app.
 * O Expo só injeta variáveis com prefixo EXPO_PUBLIC_ e exige acesso estático
 * (process.env.EXPO_PUBLIC_X), por isso cada uma é lida explicitamente abaixo.
 */
const schema = z.object({
  EXPO_PUBLIC_API_PROTOCOL: z.enum(['http', 'https']).default('http'),
  EXPO_PUBLIC_API_HOST: z.string().trim().optional(),
  EXPO_PUBLIC_API_PORT: z.coerce.number().int().positive().optional(),
  EXPO_PUBLIC_API_PREFIX: z.string().trim().default('/api/v1'),
  EXPO_PUBLIC_API_TIMEOUT_MS: z.coerce.number().int().positive().default(10_000),
  EXPO_PUBLIC_STATUS_REFRESH_MS: z.coerce.number().int().positive().default(120_000),
});

const vazioParaUndefined = (valor: string | undefined) =>
  valor === undefined || valor.trim() === '' ? undefined : valor;

const parsed = schema.safeParse({
  EXPO_PUBLIC_API_PROTOCOL: vazioParaUndefined(process.env.EXPO_PUBLIC_API_PROTOCOL),
  EXPO_PUBLIC_API_HOST: vazioParaUndefined(process.env.EXPO_PUBLIC_API_HOST),
  EXPO_PUBLIC_API_PORT: vazioParaUndefined(process.env.EXPO_PUBLIC_API_PORT),
  EXPO_PUBLIC_API_PREFIX: vazioParaUndefined(process.env.EXPO_PUBLIC_API_PREFIX),
  EXPO_PUBLIC_API_TIMEOUT_MS: vazioParaUndefined(process.env.EXPO_PUBLIC_API_TIMEOUT_MS),
  EXPO_PUBLIC_STATUS_REFRESH_MS: vazioParaUndefined(process.env.EXPO_PUBLIC_STATUS_REFRESH_MS),
});

if (!parsed.success) {
  throw new Error(
    `Variáveis de ambiente inválidas (veja .env.example):\n${z.prettifyError(parsed.error)}`,
  );
}

const vars = parsed.data;

/**
 * Sem host configurado, usa o IP da máquina que serve o bundle (Metro). Assim o app
 * funciona no Expo Go em celular físico sem editar o .env. No emulador Android,
 * "localhost" aponta para o próprio emulador, então caímos em 10.0.2.2.
 */
function resolverHost(): string {
  if (vars.EXPO_PUBLIC_API_HOST) {
    return vars.EXPO_PUBLIC_API_HOST;
  }
  const hostMetro = Constants.expoConfig?.hostUri?.split(':')[0];
  if (hostMetro && hostMetro !== 'localhost' && hostMetro !== '127.0.0.1') {
    return hostMetro;
  }
  return Platform.OS === 'android' ? '10.0.2.2' : 'localhost';
}

function normalizarPrefixo(prefixo: string): string {
  if (!prefixo) return '';
  const comBarra = prefixo.startsWith('/') ? prefixo : `/${prefixo}`;
  return comBarra.replace(/\/+$/, '');
}

const porta = vars.EXPO_PUBLIC_API_PORT ? `:${vars.EXPO_PUBLIC_API_PORT}` : '';

export const env = {
  apiBaseUrl: `${vars.EXPO_PUBLIC_API_PROTOCOL}://${resolverHost()}${porta}${normalizarPrefixo(vars.EXPO_PUBLIC_API_PREFIX)}`,
  apiTimeoutMs: vars.EXPO_PUBLIC_API_TIMEOUT_MS,
  statusRefreshMs: vars.EXPO_PUBLIC_STATUS_REFRESH_MS,
} as const;
