import { router, Stack } from 'expo-router';
import { ChevronRight, Plus, SearchX } from 'lucide-react-native';
import { useMemo, useState } from 'react';
import { FlatList, Pressable, RefreshControl, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { LinhaBadge } from '@/components/LinhaBadge';
import { StatusBadge } from '@/components/StatusBadge';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useRede, useTodasAsLinhas } from '@/hooks/queries';
import { colors } from '@/theme/colors';
import type { LinhaResumo, StatusOperacional } from '@/types/api';
import { TIPOS_TRANSPORTE } from '@/utils/admin';
import { compararCodigos, contemTexto } from '@/utils/text';

export default function LinhasAdminScreen() {
  const { data: linhas, isPending, error, refetch, isRefetching } = useTodasAsLinhas();
  const { data: rede, refetch: refetchRede } = useRede();
  const [busca, setBusca] = useState('');

  const statusPorLinha = useMemo(
    () => new Map<number, StatusOperacional>((rede?.linhas ?? []).map((l) => [l.id, l.status])),
    [rede],
  );
  const visiveis = useMemo(
    () =>
      (linhas ?? [])
        .filter((l) => contemTexto(l.nome, busca) || l.codigo === busca.trim())
        // Ativas primeiro; dentro de cada grupo, pelo código.
        .sort((a, b) => Number(b.ativa) - Number(a.ativa) || compararCodigos(a.codigo, b.codigo)),
    [linhas, busca],
  );

  const abrir = (linha: LinhaResumo) =>
    router.push({ pathname: '/admin/linhas/[id]', params: { id: String(linha.id) } });

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <Stack.Screen
        options={{
          headerRight: () => (
            <Button
              rotulo="Nova"
              icone={Plus}
              variante="ghost"
              compacto
              onPress={() => router.push('/admin/linhas/nova')}
            />
          ),
        }}
      />
      <View className="px-4 pb-2 pt-3">
        <SearchInput valor={busca} aoMudar={setBusca} placeholder="Buscar por nome ou número" />
      </View>
      {error && !linhas ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <FlatList
          data={visiveis}
          keyExtractor={(l) => String(l.id)}
          keyboardShouldPersistTaps="handled"
          contentContainerClassName="px-4 pb-6"
          ListEmptyComponent={
            isPending ? (
              <SkeletonLista itens={8} />
            ) : (
              <EmptyState icone={SearchX} titulo="Nenhuma linha encontrada" />
            )
          }
          renderItem={({ item }) => {
            const status = statusPorLinha.get(item.id);
            const tipo = TIPOS_TRANSPORTE.find((t) => t.valor === item.tipoTransporte)?.rotulo;
            return (
              <Pressable
                onPress={() => abrir(item)}
                accessibilityRole="button"
                accessibilityLabel={`${item.nome}${item.ativa ? '' : ', desativada'}`}
                accessibilityHint="Edita a linha"
                className={`mb-2 flex-row items-center gap-3 rounded-2xl border border-border bg-surface p-3 active:opacity-80 ${
                  item.ativa ? '' : 'opacity-70'
                }`}
              >
                <LinhaBadge codigo={item.codigo} cor={item.cor} />
                <View className="flex-1 gap-1">
                  <Text className="text-base font-medium text-ink" numberOfLines={1}>
                    {item.nome}
                  </Text>
                  <View className="flex-row flex-wrap items-center gap-1.5">
                    {item.ativa ? (
                      status && <StatusBadge status={status} />
                    ) : (
                      <Badge rotulo="Desativada" tom="neutral" />
                    )}
                    <Text className="text-xs text-ink-muted" numberOfLines={1}>
                      {[tipo, item.operadora].filter(Boolean).join(' · ')}
                    </Text>
                  </View>
                </View>
                <ChevronRight size={18} color={colors.ink.muted} />
              </Pressable>
            );
          }}
          refreshControl={
            <RefreshControl
              refreshing={isRefetching}
              onRefresh={() => {
                void refetch();
                void refetchRede();
              }}
              tintColor={colors.primary.DEFAULT}
              colors={[colors.primary.DEFAULT]}
            />
          }
        />
      )}
    </SafeAreaView>
  );
}
