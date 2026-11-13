import * as Notifications from 'expo-notifications';
import { AndroidImportance } from 'expo-notifications';
import { Platform } from 'react-native';

import { momentoDoLembrete } from '@/utils/agendamento';

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

/** Um agendamento do servidor já com os dados da rota, pronto para virar lembrete. */
export interface LembreteAgendamento {
  idAgendamento: number;
  nome: string;
  origem: string;
  destino: string;
  /** Horário de partida (HH:mm), para o texto. */
  horario: string;
  horarioPartidaMinutos: number;
  antecedenciaMinutos: number;
  /** 0 = domingo ... 6 = sábado. */
  dias: number[];
}

const PREFIXO_LEMBRETE = 'lembrete-';

/** Cancela todos os lembretes agendados (ex.: ao encerrar a sessão). */
export async function cancelarTodosOsLembretes(): Promise<void> {
  await Notifications.cancelAllScheduledNotificationsAsync().catch(() => undefined);
}

async function cancelarLembretesDeAgendamento(): Promise<void> {
  const agendadas = await Notifications.getAllScheduledNotificationsAsync().catch(() => []);
  await Promise.all(
    agendadas
      .filter((n) => n.identifier.startsWith(PREFIXO_LEMBRETE))
      .map((n) =>
        Notifications.cancelScheduledNotificationAsync(n.identifier).catch(() => undefined),
      ),
  );
}

/**
 * Recria os lembretes semanais a partir dos agendamentos (ativos) vindos do backend: um por dia
 * de cada agendamento, no horário de partida menos a antecedência. Não pede permissão: sem
 * permissão concedida, só limpa os antigos e retorna false.
 */
export async function sincronizarLembretes(lembretes: LembreteAgendamento[]): Promise<boolean> {
  await cancelarLembretesDeAgendamento();
  if (lembretes.length === 0) return true;

  const permissao = await Notifications.getPermissionsAsync().catch(() => null);
  if (!permissao?.granted) return false;

  await Promise.all(
    lembretes.flatMap((l) =>
      l.dias.map((dia) => {
        const momento = momentoDoLembrete(dia, l.horarioPartidaMinutos, l.antecedenciaMinutos);
        return Notifications.scheduleNotificationAsync({
          identifier: `${PREFIXO_LEMBRETE}${l.idAgendamento}-${dia}`,
          content: {
            title:
              l.antecedenciaMinutos > 0
                ? `${l.nome}: saída às ${l.horario}`
                : `Hora de sair: ${l.nome}`,
            body: `${l.origem} → ${l.destino}. Confira o status das linhas.`,
            data: { idAgendamento: l.idAgendamento },
          },
          trigger: {
            type: Notifications.SchedulableTriggerInputTypes.WEEKLY,
            // expo-notifications usa 1 = domingo.
            weekday: momento.dia + 1,
            hour: momento.hora,
            minute: momento.minuto,
            channelId: CANAL_ANDROID,
          },
        });
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
