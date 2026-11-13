import { useEffect, useRef, useState } from 'react';
import { AppState } from 'react-native';

import { log } from '@/lib/log';
import { type LembreteAgendamento, sincronizarLembretes } from '@/services/notifications';
import { useLogado } from '@/store/sessao';
import { diaParaIndice } from '@/utils/agendamento';

import { useRotasDoUsuario } from './useRotasDoUsuario';

/**
 * Mantém os lembretes do aparelho iguais aos agendamentos da conta no backend: a cada mudança
 * (criou, editou, pausou, excluiu, ou entrou em outro aparelho) recria as notificações locais.
 * Também ressincroniza ao voltar para o app, caso a permissão de notificação tenha sido concedida
 * nas configurações. Montado uma única vez no layout raiz.
 */
export function useLembretesAgendamentos(): void {
  const logado = useLogado();
  const { rotas, isPending, agendamentosCarregando } = useRotasDoUsuario();
  const ultimaAssinatura = useRef<string | null>(null);

  const lembretes: LembreteAgendamento[] = rotas.flatMap((rota) =>
    rota.agendamentos
      .filter((a) => a.ativa)
      .map((a) => ({
        idAgendamento: a.id,
        nome: rota.nome,
        origem: rota.origem.nome,
        destino: rota.destino.nome,
        horario: a.horario,
        horarioPartidaMinutos: a.horarioPartidaMinutos,
        antecedenciaMinutos: a.antecedenciaMinutos,
        dias: a.diasSemana.map(diaParaIndice),
      })),
  );
  // Muda a cada volta ao app: a permissão de notificação pode ter sido concedida nas configurações.
  const [retornos, setRetornos] = useState(0);
  const assinatura = `${retornos}|${JSON.stringify(lembretes)}`;
  const carregado = logado && !isPending && !agendamentosCarregando;

  useEffect(() => {
    // Sem login, o logout (encerrarSessao) já cancela tudo; não mexe enquanto carrega.
    if (!carregado || assinatura === ultimaAssinatura.current) return;
    ultimaAssinatura.current = assinatura;
    const dados = JSON.parse(
      assinatura.slice(assinatura.indexOf('|') + 1),
    ) as LembreteAgendamento[];
    void sincronizarLembretes(dados)
      .then((ok) => log.info('lembretes', ok ? 'sincronizados' : 'sem permissão de notificação'))
      .catch((erro: unknown) => log.erro('lembretes', 'falha ao sincronizar', erro));
  }, [carregado, assinatura]);

  useEffect(() => {
    const inscricao = AppState.addEventListener('change', (estado) => {
      if (estado === 'active') setRetornos((n) => n + 1);
    });
    return () => inscricao.remove();
  }, []);
}
