import * as Notifications from 'expo-notifications';
import { AndroidImportance } from 'expo-notifications';
import { Platform } from 'react-native';

import type { Agendamento } from '@/store/rotasLocais';

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

/** Dados mínimos de uma rota salva para montar o lembrete. */
export interface DadosLembrete {
  rotaId: number;
  nome: string;
  origem: string;
  destino: string;
  agendamento: Agendamento | null;
}

const identificadorLembrete = (rotaId: number, dia: number) => `lembrete-${rotaId}-${dia}`;

export async function cancelarLembretes(rotaId: number): Promise<void> {
  await Promise.all(
    Array.from({ length: 7 }, (_, dia) =>
      Notifications.cancelScheduledNotificationAsync(identificadorLembrete(rotaId, dia)).catch(
        () => undefined,
      ),
    ),
  );
}

/** Cancela todos os lembretes agendados (ex.: ao encerrar a sessão). */
export async function cancelarTodosOsLembretes(): Promise<void> {
  await Notifications.cancelAllScheduledNotificationsAsync().catch(() => undefined);
}

/** Agenda um lembrete semanal para cada dia do agendamento da rota (substitui os anteriores). */
export async function agendarLembretes(rota: DadosLembrete): Promise<boolean> {
  await cancelarLembretes(rota.rotaId);
  if (!rota.agendamento || rota.agendamento.dias.length === 0) return true;

  const permitido = await pedirPermissaoNotificacoes();
  if (!permitido) return false;

  const [hour, minute] = rota.agendamento.horario.split(':').map(Number);
  await Promise.all(
    rota.agendamento.dias.map((dia) =>
      Notifications.scheduleNotificationAsync({
        identifier: identificadorLembrete(rota.rotaId, dia),
        content: {
          title: `Hora de sair: ${rota.nome}`,
          body: `${rota.origem} → ${rota.destino}. Confira o status das linhas.`,
          data: { rotaId: rota.rotaId },
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
