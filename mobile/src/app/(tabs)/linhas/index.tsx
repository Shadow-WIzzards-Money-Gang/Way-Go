import { router } from 'expo-router';
import { SearchX } from 'lucide-react-native';
import { useCallback, useMemo, useState } from 'react';
import { FlatList, RefreshControl, View } from 'react-native';

import { CabecalhoAba } from '@/components/CabecalhoAba';
import { LinhaStatusItem } from '@/components/LinhaStatusItem';
import { Chip } from '@/components/ui/Chip';
import { Screen } from '@/components/ui/Screen';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useRede } from '@/hooks/queries';
import { extremosDaLinha, useRedeIndex } from '@/hooks/useRedeIndex';
import { colors } from '@/theme/colors';
import type { LinhaRede, TipoTransporte } from '@/types/api';
import { contemTexto } from '@/utils/text';

const FILTROS: { valor: TipoTransporte | null; rotulo: string }[] = [
  { valor: null, rotulo: 'Todas' },
  { valor: 'METRO', rotulo: 'Metrô' },
  { valor: 'TREM_METROPOLITANO', rotulo: 'Trem' },
  { valor: 'MONOTRILHO', rotulo: 'Monotrilho' },
];

export default function LinhasScreen() {
  const { data: rede, isPending, error, refetch, isRefetching } = useRede();
  const index = useRedeIndex(rede);
  const [busca, setBusca] = useState('');
  const [tipo, setTipo] = useState<TipoTransporte | null>(null);

  const linhas = useMemo(
    () =>
      (rede?.linhas ?? [])
        .filter((l) => (tipo ? l.tipoTransporte === tipo : true))
        .filter((l) => contemTexto(l.nome, busca) || l.codigo === busca.trim())
        .sort((a, b) => a.codigo.localeCompare(b.codigo, 'pt-BR', { numeric: true })),
    [rede, busca, tipo],
  );

  const abrirLinha = useCallback(
    (linha: LinhaRede) =>
      router.push({ pathname: '/linhas/[id]', params: { id: String(linha.id) } }),
    [],
  );

  return (
    <Screen>
      <View className="gap-3 px-4 pb-3 pt-4">
        <CabecalhoAba titulo="Linhas" />
        <SearchInput
          valor={busca}
          aoMudar={setBusca}
          placeholder="Buscar linha por nome ou número"
        />
        <View className="flex-row flex-wrap gap-2">
          {FILTROS.map((f) => (
            <Chip
              key={f.rotulo}
              rotulo={f.rotulo}
              selecionado={tipo === f.valor}
              onPress={() => setTipo(f.valor)}
            />
          ))}
        </View>
      </View>

      {error && !rede ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <FlatList
          data={linhas}
          keyExtractor={(l) => String(l.id)}
          keyboardShouldPersistTaps="handled"
          contentContainerClassName="px-4 pb-6"
          ListEmptyComponent={
            isPending ? (
              <SkeletonLista itens={7} />
            ) : (
              <EmptyState icone={SearchX} titulo="Nenhuma linha encontrada" />
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
      )}
    </Screen>
  );
}
