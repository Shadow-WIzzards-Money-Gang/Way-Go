import { create } from 'zustand';

interface PushState {
  /** Token FCM já registrado no backend para a conta logada; null = sem push remoto. */
  tokenRegistrado: string | null;
  definirTokenRegistrado: (token: string | null) => void;
}

/**
 * Com o push remoto (FCM) ativo, o backend manda os lembretes e os alertas de status,
 * e as notificações locais (reserva para o Expo Go e o iOS) ficam desligadas.
 */
export const usePush = create<PushState>((set) => ({
  tokenRegistrado: null,
  definirTokenRegistrado: (tokenRegistrado) => set({ tokenRegistrado }),
}));

export const usePushAtivo = () => usePush((s) => s.tokenRegistrado !== null);
