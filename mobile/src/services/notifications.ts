import * as Notifications from 'expo-notifications';
import { AndroidImportance } from 'expo-notifications';
import { Platform } from 'react-native';

import type { RotaSalva } from '@/store/rotasSalvas';

const CANAL_ANDROID = 'waygo-alertas';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: false,
    shouldSetBadge: false,
  }),
});

export async function pedirPermissaoNotificacoes(): Promise<boolean> {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync(CANAL_ANDROID, {
      name: 'Alertas das rotas',
      importance: AndroidImportance.HIGH,
    });
  }
  const atual = await Notifications.getPermissionsAsync();
  if (atual.granted) return true;
  const pedido = await Notifications.requestPermissionsAsync();
  return pedido.granted;
}

const identificadorLembrete = (rotaId: string, dia: number) => `lembrete-${rotaId}-${dia}`;

export async function cancelarLembretes(rotaId: string): Promise<void> {
  await Promise.all(
    Array.from({ length: 7 }, (_, dia) =>
      Notifications.cancelScheduledNotificationAsync(identificadorLembrete(rotaId, dia)).catch(
        () => undefined,
      ),
    ),
  );
}

/** Agenda um lembrete semanal para cada dia do agendamento da rota (substitui os anteriores). */
export async function agendarLembretes(rota: RotaSalva): Promise<boolean> {
  await cancelarLembretes(rota.id);
  if (!rota.agendamento || rota.agendamento.dias.length === 0) return true;

  const permitido = await pedirPermissaoNotificacoes();
  if (!permitido) return false;

  const [hour, minute] = rota.agendamento.horario.split(':').map(Number);
  await Promise.all(
    rota.agendamento.dias.map((dia) =>
      Notifications.scheduleNotificationAsync({
        identifier: identificadorLembrete(rota.id, dia),
        content: {
          title: `Hora de sair: ${rota.nome}`,
          body: `${rota.origem.nome} → ${rota.destino.nome}. Confira o status das linhas.`,
          data: { rotaId: rota.id },
        },
        trigger: {
          type: Notifications.SchedulableTriggerInputTypes.WEEKLY,
          // expo-notifications usa 1 = domingo.
          weekday: dia + 1,
          hour,
          minute,
          channelId: CANAL_ANDROID,
        },
      }),
    ),
  );
  return true;
}

export async function notificarAgora(titulo: string, corpo: string): Promise<void> {
  await Notifications.scheduleNotificationAsync({
    content: { title: titulo, body: corpo },
    trigger: null,
  });
}
