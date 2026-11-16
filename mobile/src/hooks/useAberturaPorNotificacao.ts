import * as Notifications from 'expo-notifications';
import { router } from 'expo-router';
import { useEffect, useRef } from 'react';

import { hrefComParams } from '@/lib/conta';
import { usePlanejamento } from '@/store/planejamento';
import { chaveDoTrajeto, rotaDaNotificacao } from '@/utils/notificacao';

/** No Android, o "data" do FCM pode vir no conteúdo ou só na RemoteMessage original. */
function dadosDa(resposta: Notifications.NotificationResponse): Record<string, unknown> | null {
  const { content, trigger } = resposta.notification.request;
  if (content.data && Object.keys(content.data).length > 0) return content.data;
  const remota =
    trigger && typeof trigger === 'object' && 'remoteMessage' in trigger
      ? (trigger.remoteMessage as { data?: Record<string, unknown> } | undefined)
      : undefined;
  return remota?.data ?? null;
}

/**
 * Ao tocar num aviso do servidor (lembrete ou alerta de status), abre a rota no mapa;
 * se o aviso ofereceu rota alternativa, o mapa já abre nela (US16).
 * Funciona com o app aberto, em segundo plano ou fechado. Montado no layout raiz.
 */
export function useAberturaPorNotificacao(): void {
  const resposta = Notifications.useLastNotificationResponse();
  const tratada = useRef<string | null>(null);

  useEffect(() => {
    if (!resposta || resposta.actionIdentifier !== Notifications.DEFAULT_ACTION_IDENTIFIER) return;
    const id = resposta.notification.request.identifier;
    if (tratada.current === id) return;
    tratada.current = id;

    const rota = rotaDaNotificacao(dadosDa(resposta));
    if (!rota) return;
    const plano = usePlanejamento.getState();
    plano.definirEstacao('origem', rota.origem);
    plano.definirEstacao('destino', rota.destino);
    plano.definirCriterio(rota.criterio);
    router.navigate(
      hrefComParams('/', {
        alternativa: rota.alternativa
          ? chaveDoTrajeto(rota.origem.nome, rota.destino.nome, rota.criterio)
          : undefined,
      }) as '/',
    );
  }, [resposta]);
}
