import * as Notifications from 'expo-notifications';
import { useEffect, useState } from 'react';
import { AppState } from 'react-native';

import { dispositivosApi } from '@/api/waygo';
import { log } from '@/lib/log';
import { obterTokenFcm } from '@/services/notifications';
import { usePush } from '@/store/push';
import { useRotasLocais } from '@/store/rotasLocais';
import { useLogado } from '@/store/sessao';

/**
 * Registra o aparelho no backend (POST /dispositivos) para receber push pelo FCM:
 * ao entrar, quando o Firebase renova o token e quando o switch de alertas muda.
 * Se não houver token (Expo Go, iOS, build sem Firebase) ou o registro falhar,
 * o push fica desligado e as notificações locais continuam valendo.
 * Montado uma única vez no layout raiz.
 */
export function usePushRemoto(): void {
  const logado = useLogado();
  const alertasAtivos = useRotasLocais((s) => s.alertasAtivos);
  const [token, setToken] = useState<string | null>(null);
  // Muda a cada volta ao app, para tentar de novo um registro que falhou (ex.: sem internet).
  const [retornos, setRetornos] = useState(0);

  useEffect(() => {
    if (!logado) return;
    let ativo = true;
    void obterTokenFcm().then((novo) => {
      if (ativo) setToken(novo);
    });
    const inscricao = Notifications.addPushTokenListener((renovado) => {
      if (typeof renovado.data === 'string') setToken(renovado.data);
    });
    return () => {
      ativo = false;
      inscricao.remove();
    };
  }, [logado]);

  useEffect(() => {
    const { tokenRegistrado, definirTokenRegistrado } = usePush.getState();
    if (!logado || !token) {
      if (tokenRegistrado) definirTokenRegistrado(null);
      return;
    }
    let ativo = true;
    dispositivosApi
      .registrar({ token, plataforma: 'ANDROID', alertasStatus: alertasAtivos })
      .then(() => {
        if (!ativo) return;
        definirTokenRegistrado(token);
        log.info('push', 'aparelho registrado para notificações do servidor');
      })
      .catch((erro: unknown) => {
        if (ativo) definirTokenRegistrado(null);
        log.erro('push', 'falha ao registrar o aparelho; usando notificações locais', erro);
      });
    return () => {
      ativo = false;
    };
  }, [logado, token, alertasAtivos, retornos]);

  useEffect(() => {
    const inscricao = AppState.addEventListener('change', (estado) => {
      if (estado === 'active' && usePush.getState().tokenRegistrado === null) setRetornos((n) => n + 1);
    });
    return () => inscricao.remove();
  }, []);
}
