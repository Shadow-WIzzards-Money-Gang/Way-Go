import { useEffect, useRef } from 'react';

import { notificarAgora } from '@/services/notifications';
import { useRotasSalvas } from '@/store/rotasSalvas';
import type { StatusOperacional } from '@/types/api';
import { infoStatus } from '@/utils/status';
import { formatarNomeLinha } from '@/utils/text';

import { useRede } from './queries';

/**
 * Enquanto o app está aberto, compara o status das linhas das rotas salvas a cada
 * atualização de /rede e dispara uma notificação local quando algum muda.
 * Montado uma única vez no layout raiz.
 */
export function useAlertasRotas(): void {
  const { data: rede } = useRede();
  const rotas = useRotasSalvas((s) => s.rotas);
  const alertasAtivos = useRotasSalvas((s) => s.alertasAtivos);
  const anteriores = useRef<Map<number, StatusOperacional> | null>(null);

  useEffect(() => {
    if (!rede) return;
    const atuais = new Map(rede.linhas.map((l) => [l.id, l.status]));
    const antes = anteriores.current;
    anteriores.current = atuais;

    if (!antes || !alertasAtivos) return;

    const linhasMonitoradas = new Set(rotas.flatMap((r) => r.linhas.map((l) => l.id)));
    for (const linha of rede.linhas) {
      const statusAnterior = antes.get(linha.id);
      if (!linhasMonitoradas.has(linha.id) || !statusAnterior || statusAnterior === linha.status) {
        continue;
      }
      const afetadas = rotas.filter((r) => r.linhas.some((l) => l.id === linha.id));
      void notificarAgora(
        `${formatarNomeLinha(linha.nome)}: ${infoStatus(linha.status).rotulo}`,
        `Afeta ${afetadas.map((r) => r.nome).join(', ')}.${linha.statusDescricao ? ` ${linha.statusDescricao}` : ''}`,
      ).catch(() => undefined);
    }
  }, [rede, rotas, alertasAtivos]);
}
