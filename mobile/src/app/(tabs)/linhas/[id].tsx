import { router, useLocalSearchParams } from 'expo-router';
import { ChevronLeft, MapPinOff, TriangleAlert } from 'lucide-react-native';
import { useMemo, useState } from 'react';
import { Alert, FlatList, Pressable, RefreshControl, Text, View } from 'react-native';

import { EstacaoTimelineItem } from '@/components/EstacaoTimelineItem';
import { LinhaBadge } from '@/components/LinhaBadge';
import { StatusBadge } from '@/components/StatusBadge';
import { Card } from '@/components/ui/Card';
import { Screen } from '@/components/ui/Screen';
import { SearchInput } from '@/components/ui/SearchInput';
import { Skeleton, SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useLinha, useLinhaStatus, useRede } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { colors } from '@/theme/colors';
import { formatarHora } from '@/utils/format';
import { infoStatus } from '@/utils/status';
import { contemTexto, formatarNomeLinha } from '@/utils/text';

export default function LinhaScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const linhaId = Number(id);

  const linhaQuery = useLinha(linhaId);
  const statusQuery = useLinhaStatus(linhaId);
  const { data: rede } = useRede();
  const index = useRedeIndex(rede);
  const [busca, setBusca] = useState('');

  const linha = linhaQuery.data;
  const status = statusQuery.data;

  const estacoes = useMemo(
    () => [...(linha?.estacoes ?? [])].sort((a, b) => a.sequencia - b.sequencia),
    [linha],
  );
  const filtradas = useMemo(
    () => estacoes.filter((e) => contemTexto(e.nome, busca)),
    [estacoes, busca],
  );

  /** Códigos das outras linhas que passam pela estação (chips "L3 · L7"). */
  const outrasLinhas = (estacaoId: number): string[] =>
    (index.estacaoPorId.get(estacaoId)?.linhaIds ?? [])
      .filter((outra) => outra !== linhaId)
      .map((outra) => index.linhaPorId.get(outra)?.codigo)
      .filter((c): c is string => Boolean(c))
      .sort((a, b) => a.localeCompare(b, 'pt-BR', { numeric: true }));

  const relatar = () =>
    Alert.alert(
      'Relatar problema',
      'Em breve você poderá avisar sobre lotação, atrasos e falhas nesta linha.',
    );

  const voltar = () => (router.canGoBack() ? router.back() : router.replace('/linhas'));

  const atualizadoAs = formatarHora(status?.atualizadoEm);
  const filtrando = busca.trim().length > 0;

  return (
    <Screen>
      <View className="flex-row items-center gap-2 px-2 pb-2 pt-2">
        <Pressable
          onPress={voltar}
          accessibilityRole="button"
          accessibilityLabel="Voltar"
          className="h-11 w-11 items-center justify-center rounded-full active:bg-neutral-soft"
        >
          <ChevronLeft size={24} color={colors.ink.DEFAULT} />
        </Pressable>
        {linha ? (
          <View className="flex-1 flex-row items-center gap-2">
            <LinhaBadge codigo={linha.codigo} cor={linha.cor} />
            <Text
              className="flex-1 text-xl font-bold text-ink"
              numberOfLines={1}
              accessibilityRole="header"
            >
              {formatarNomeLinha(linha.nome)}
            </Text>
          </View>
        ) : (
          <Skeleton className="h-7 flex-1" />
        )}
        <Pressable
          onPress={relatar}
          accessibilityRole="button"
          accessibilityLabel="Relatar problema na linha"
          className="min-h-[44px] flex-row items-center gap-1 rounded-full px-3 active:bg-danger-soft"
        >
          <TriangleAlert size={16} color={colors.danger.DEFAULT} />
          <Text className="font-semibold text-danger">Relatar</Text>
        </Pressable>
      </View>

      {linhaQuery.error && !linha ? (
        <ErrorState erro={linhaQuery.error} aoTentarNovamente={() => void linhaQuery.refetch()} />
      ) : (
        <FlatList
          data={filtradas}
          keyExtractor={(e) => String(e.id)}
          keyboardShouldPersistTaps="handled"
          contentContainerClassName="px-4 pb-8"
          ListHeaderComponent={
            <View className="gap-4 pb-3 pt-2">
              {status ? (
                <Card>
                  <View className="flex-row items-center justify-between gap-2">
                    <StatusBadge status={status.status} />
                    {atualizadoAs && (
                      <Text className="text-xs text-ink-muted">Atualizado às {atualizadoAs}</Text>
                    )}
                  </View>
                  <Text className="mt-2 text-sm leading-5 text-ink">
                    {status.descricao ?? infoStatus(status.status).rotulo}
                  </Text>
                </Card>
              ) : statusQuery.isPending ? (
                <Skeleton className="h-20" />
              ) : null}
              <SearchInput valor={busca} aoMudar={setBusca} placeholder="Buscar estação" />
              <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
                Estações
              </Text>
            </View>
          }
          ListEmptyComponent={
            linhaQuery.isPending ? (
              <SkeletonLista itens={10} className="h-9" />
            ) : (
              <EmptyState icone={MapPinOff} titulo="Nenhuma estação encontrada" />
            )
          }
          renderItem={({ item, index: i }) => (
            <EstacaoTimelineItem
              nome={item.nome}
              cor={linha?.cor ?? colors.primary.DEFAULT}
              ativa={item.ativa}
              // Na busca, cada resultado é isolado: não liga com o vizinho.
              primeira={filtrando || i === 0}
              ultima={filtrando || i === filtradas.length - 1}
              destaque={i === 0 || i === filtradas.length - 1}
              outrasLinhas={outrasLinhas(item.id)}
            />
          )}
          refreshControl={
            <RefreshControl
              refreshing={linhaQuery.isRefetching || statusQuery.isRefetching}
              onRefresh={() => {
                void linhaQuery.refetch();
                void statusQuery.refetch();
              }}
              tintColor={colors.primary.DEFAULT}
              colors={[colors.primary.DEFAULT]}
            />
          }
        />
      )}
    </Screen>
  );
}
