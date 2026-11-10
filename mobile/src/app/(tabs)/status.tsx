import { router } from 'expo-router';
import { Bell, TrainTrack } from 'lucide-react-native';
import { useCallback, useMemo } from 'react';
import { FlatList, Pressable, RefreshControl, Text, View } from 'react-native';

import { AvisoBanner } from '@/components/AvisoBanner';
import { LinhaStatusItem } from '@/components/LinhaStatusItem';
import { Screen } from '@/components/ui/Screen';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useRede } from '@/hooks/queries';
import { extremosDaLinha, type RedeIndex, useRedeIndex } from '@/hooks/useRedeIndex';
import { type RotaSalva, useRotasSalvas } from '@/store/rotasSalvas';
import { colors } from '@/theme/colors';
import type { LinhaRede } from '@/types/api';
import { formatarHora } from '@/utils/format';
import { ehStatusNormal, infoStatus } from '@/utils/status';
import { formatarNomeLinha } from '@/utils/text';

/** Ordena pelo código numérico da linha (1, 2, ... 15), como nos protótipos. */
const porCodigo = (a: LinhaRede, b: LinhaRede) =>
  a.codigo.localeCompare(b.codigo, 'pt-BR', { numeric: true });

/** Primeira rota salva afetada por alguma linha com problema (banner do topo). */
function encontrarRotaAfetada(rotas: RotaSalva[], index: RedeIndex) {
  for (const rota of rotas) {
    for (const l of rota.linhas) {
      const linha = index.linhaPorId.get(l.id);
      if (linha && !ehStatusNormal(linha.status)) return { rota, linha };
    }
  }
  return null;
}

export default function StatusScreen() {
  const { data: rede, isPending, error, refetch, isRefetching, dataUpdatedAt } = useRede();
  const index = useRedeIndex(rede);
  const rotas = useRotasSalvas((s) => s.rotas);
  const alertasAtivos = useRotasSalvas((s) => s.alertasAtivos);

  const linhas = useMemo(() => [...(rede?.linhas ?? [])].sort(porCodigo), [rede]);

  const alerta = encontrarRotaAfetada(rotas, index);

  const abrirLinha = useCallback(
    (linha: LinhaRede) =>
      router.push({ pathname: '/linhas/[id]', params: { id: String(linha.id) } }),
    [],
  );

  const atualizadoAs = dataUpdatedAt ? formatarHora(new Date(dataUpdatedAt).toISOString()) : null;

  const cabecalho = (
    <View className="gap-4 pb-4">
      <View className="flex-row items-start justify-between">
        <View className="flex-1">
          <Text className="text-3xl font-bold text-ink" accessibilityRole="header">
            Status das linhas
          </Text>
          {atualizadoAs && (
            <Text className="mt-1 text-sm text-ink-muted">
              Atualizado às {atualizadoAs} · fonte Trilhos Agora
            </Text>
          )}
        </View>
        <Pressable
          onPress={() => router.navigate('/rotas')}
          accessibilityRole="button"
          accessibilityLabel={
            alertasAtivos ? 'Alertas das rotas ativados' : 'Configurar alertas das rotas'
          }
          className="h-11 w-11 items-center justify-center rounded-full border border-border bg-surface active:opacity-70"
        >
          <Bell size={20} color={colors.ink.DEFAULT} />
          {alerta && (
            <View className="absolute right-2.5 top-2.5 h-2.5 w-2.5 rounded-full border-2 border-surface bg-danger" />
          )}
        </Pressable>
      </View>

      {alerta && (
        <AvisoBanner
          titulo={`Atenção na sua rota ${alerta.rota.nome}`}
          mensagem={`${formatarNomeLinha(alerta.linha.nome)}: ${
            alerta.linha.statusDescricao ?? infoStatus(alerta.linha.status).rotulo
          }`}
        />
      )}

      <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
        Todas as linhas
      </Text>
    </View>
  );

  if (error && !rede) {
    return (
      <Screen className="px-4 pt-4">
        {cabecalho}
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      </Screen>
    );
  }

  return (
    <Screen>
      <FlatList
        data={linhas}
        keyExtractor={(l) => String(l.id)}
        contentContainerClassName="px-4 pt-4 pb-6"
        ListHeaderComponent={cabecalho}
        ListEmptyComponent={
          isPending ? (
            <SkeletonLista itens={7} />
          ) : (
            <EmptyState icone={TrainTrack} titulo="Nenhuma linha cadastrada" />
          )
        }
        renderItem={({ item }) => (
          <LinhaStatusItem
            linha={item}
            extremos={extremosDaLinha(item, index)}
            onPress={abrirLinha}
          />
        )}
        refreshControl={
          <RefreshControl
            refreshing={isRefetching}
            onRefresh={() => void refetch()}
            tintColor={colors.primary.DEFAULT}
            colors={[colors.primary.DEFAULT]}
          />
        }
      />
    </Screen>
  );
}
