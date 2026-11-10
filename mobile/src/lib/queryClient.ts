import { focusManager, onlineManager, QueryClient } from '@tanstack/react-query';
import { AppState, Platform } from 'react-native';

import { ApiError } from '@/api/errors';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // Erros 4xx (rota inexistente, estação não encontrada) não melhoram com nova tentativa.
      retry: (tentativas, erro) =>
        !(erro instanceof ApiError && erro.status !== undefined && erro.status < 500) &&
        tentativas < 2,
    },
  },
});

/** Revalida as queries quando o app volta para o primeiro plano. */
export function configurarFocoDoApp(): () => void {
  if (Platform.OS === 'web') return () => undefined;
  const assinatura = AppState.addEventListener('change', (estado) => {
    focusManager.setFocused(estado === 'active');
  });
  onlineManager.setOnline(true);
  return () => assinatura.remove();
}
