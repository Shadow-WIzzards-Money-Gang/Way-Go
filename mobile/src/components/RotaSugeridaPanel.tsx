import { ArrowRightLeft, CalendarClock, Route, SearchX } from 'lucide-react-native';
import { useState } from 'react';
import { Pressable, ScrollView, Text, View, useWindowDimensions } from 'react-native';
import Animated, { FadeIn } from 'react-native-reanimated';

import { ApiError } from '@/api/errors';
import { colors } from '@/theme/colors';
import type { CriterioRota, Rota, TrechoRota } from '@/types/api';
import { formatarMinutos, pluralizar } from '@/utils/format';
import { formatarNomeLinha } from '@/utils/text';

import { AvisoBanner } from './AvisoBanner';
import { LinhaBadge } from './LinhaBadge';
import { StatusBadge } from './StatusBadge';
import { Button } from './ui/Button';
import { Chip } from './ui/Chip';
import { Skeleton } from './ui/Skeleton';
import { EmptyState, ErrorState } from './ui/States';

export const CRITERIOS: { valor: CriterioRota; rotulo: string }[] = [
  { valor: 'EQUILIBRADO', rotulo: 'Equilibrado' },
  { valor: 'MENOR_TEMPO', rotulo: 'Menor tempo' },
  { valor: 'MENOS_BALDEACOES', rotulo: 'Menos baldeações' },
];

interface RotaSugeridaPanelProps {
  temSelecao: boolean;
  rota: Rota | undefined;
  carregando: boolean;
  erro: unknown;
  criterio: CriterioRota;
  aoMudarCriterio: (c: CriterioRota) => void;
  aoTentarNovamente: () => void;
  aoAgendar: () => void;
  aoSalvar: () => void;
}

function contarEstacoes(rota: Rota): number {
  return new Set(rota.trechos.flatMap((t) => t.estacoes.map((e) => e.id))).size;
}

function CardTrecho({ trecho }: { trecho: TrechoRota }) {
  return (
    <View className="flex-row items-center gap-3 rounded-xl bg-background p-3">
      <LinhaBadge codigo={trecho.linha.codigo} cor={trecho.linha.cor} />
      <View className="flex-1">
        <Text className="font-semibold text-ink" numberOfLines={1}>
          {formatarNomeLinha(trecho.linha.nome)}
        </Text>
        <Text className="text-sm text-ink-muted" numberOfLines={1}>
          {trecho.embarque.nome} → {trecho.desembarque.nome}
        </Text>
        <Text className="mt-0.5 text-xs text-ink-subtle" numberOfLines={1}>
          Sentido {trecho.sentido.nome} ·{' '}
          {pluralizar(trecho.quantidadeParadas, 'parada', 'paradas')} ·{' '}
          {formatarMinutos(Math.max(1, Math.round(trecho.tempoSegundos / 60)))}
        </Text>
      </View>
      {trecho.statusLinha !== 'NORMAL' && <StatusBadge status={trecho.statusLinha} />}
    </View>
  );
}

function ConteudoRota({ rota }: { rota: Rota }) {
  const linhas = new Set(rota.trechos.map((t) => t.linha.id)).size;
  return (
    <Animated.View entering={FadeIn.duration(200)}>
      <View className="gap-3">
        <View className="flex-row items-end justify-between">
          <View>
            <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
              Rota sugerida
            </Text>
            <Text
              className="text-3xl font-bold text-ink"
              accessibilityLabel={`Tempo estimado ${rota.tempoEstimadoMinutos} minutos`}
            >
              {formatarMinutos(rota.tempoEstimadoMinutos)}
            </Text>
          </View>
          <Text className="mb-1 text-sm text-ink-muted">
            {pluralizar(linhas, 'linha', 'linhas')} ·{' '}
            {pluralizar(contarEstacoes(rota), 'estação', 'estações')}
          </Text>
        </View>

        {rota.trechos.map((trecho, i) => {
          const baldeacao = rota.baldeacoes[i];
          return (
            <View key={trecho.ordem} className="gap-2">
              <CardTrecho trecho={trecho} />
              {i < rota.trechos.length - 1 && baldeacao && (
                <View className="flex-row items-center gap-2 px-3">
                  <ArrowRightLeft size={14} color={colors.ink.muted} />
                  <Text className="text-xs text-ink-muted">
                    Baldeação em {baldeacao.estacaoDestino.nome} ·{' '}
                    {formatarMinutos(Math.max(1, Math.round(baldeacao.tempoSegundos / 60)))}
                  </Text>
                </View>
              )}
            </View>
          );
        })}

        {rota.avisos.map((aviso) => (
          <AvisoBanner key={aviso} mensagem={aviso} />
        ))}
      </View>
    </Animated.View>
  );
}

/** Painel inferior do mapa com a rota calculada. Toque na alça para recolher/expandir. */
export function RotaSugeridaPanel({
  temSelecao,
  rota,
  carregando,
  erro,
  criterio,
  aoMudarCriterio,
  aoTentarNovamente,
  aoAgendar,
  aoSalvar,
}: RotaSugeridaPanelProps) {
  const [expandido, setExpandido] = useState(true);
  const { height } = useWindowDimensions();

  let conteudo;
  if (!temSelecao) {
    conteudo = (
      <EmptyState
        icone={Route}
        titulo="Para onde vamos?"
        mensagem="Escolha a estação de origem e a de destino para ver a melhor rota."
      />
    );
  } else if (erro instanceof ApiError && erro.tipo === 'sem-rota') {
    conteudo = (
      <EmptyState
        icone={SearchX}
        titulo="Nenhuma rota disponível"
        mensagem={`${erro.message} Pode haver linhas paralisadas no caminho.`}
      />
    );
  } else if (erro && !rota) {
    conteudo = <ErrorState erro={erro} aoTentarNovamente={aoTentarNovamente} />;
  } else if (!rota) {
    conteudo = (
      <View className="gap-3">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-9 w-24" />
        <Skeleton className="h-16" />
      </View>
    );
  } else {
    conteudo = <ConteudoRota rota={rota} />;
  }

  return (
    <View
      className="rounded-t-3xl bg-surface px-4 pb-4"
      style={{
        shadowColor: '#0F172A',
        shadowOpacity: 0.1,
        shadowRadius: 20,
        shadowOffset: { width: 0, height: -4 },
        elevation: 12,
      }}
    >
      <Pressable
        onPress={() => setExpandido((v) => !v)}
        accessibilityRole="button"
        accessibilityLabel={expandido ? 'Recolher detalhes da rota' : 'Expandir detalhes da rota'}
        className="items-center py-3"
        hitSlop={8}
      >
        <View className="h-1.5 w-10 rounded-full bg-border" />
      </Pressable>

      {expandido && (
        <>
          {temSelecao && (
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerClassName="gap-2 pb-3"
            >
              {CRITERIOS.map((c) => (
                <Chip
                  key={c.valor}
                  rotulo={c.rotulo}
                  selecionado={criterio === c.valor}
                  onPress={() => aoMudarCriterio(c.valor)}
                />
              ))}
            </ScrollView>
          )}
          <ScrollView style={{ maxHeight: height * 0.42 }} showsVerticalScrollIndicator={false}>
            <View className={carregando && rota ? 'opacity-60' : ''}>{conteudo}</View>
          </ScrollView>
        </>
      )}

      {rota && temSelecao && (
        <View className="mt-4 flex-row gap-3">
          <Button
            rotulo="Agendar"
            variante="outline"
            icone={CalendarClock}
            onPress={aoAgendar}
            className="flex-1"
          />
          <Button rotulo="Salvar rota" onPress={aoSalvar} className="flex-1" />
        </View>
      )}
    </View>
  );
}
