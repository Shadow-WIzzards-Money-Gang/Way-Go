import { router } from 'expo-router';
import { SearchX } from 'lucide-react-native';
import { useCallback, useMemo, useState } from 'react';
import { FlatList, RefreshControl, Text, View } from 'react-native';

import { CabecalhoAba } from '@/components/CabecalhoAba';
import { EstacaoItem } from '@/components/EstacaoItem';
import { Chip } from '@/components/ui/Chip';
import { Screen } from '@/components/ui/Screen';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useRede } from '@/hooks/queries';
import { type RedeIndex, useRedeIndex } from '@/hooks/useRedeIndex';
import { colors } from '@/theme/colors';
import type { EstacaoRede, LinhaRede } from '@/types/api';
import { pluralizar } from '@/utils/format';
import { ehStatusNormal } from '@/utils/status';
import { contemTexto } from '@/utils/text';

type Filtro = 'todas' | 'integracoes';

const FILTROS: { valor: Filtro; rotulo: string }[] = [
  { valor: 'todas', rotulo: 'Todas' },
  { valor: 'integracoes', rotulo: 'Com integração' },
];

function linhasDa(estacao: EstacaoRede, index: RedeIndex): LinhaRede[] {
  return estacao.linhaIds
    .map((id) => index.linhaPorId.get(id))
    .filter((l): l is LinhaRede => l !== undefined)
    .sort((a, b) => a.codigo.localeCompare(b.codigo, 'pt-BR', { numeric: true }));
}

export default function EstacoesScreen() {
  const { data: rede, isPending, error, refetch, isRefetching } = useRede();
  const index = useRedeIndex(rede);
  const [busca, setBusca] = useState('');
  const [filtro, setFiltro] = useState<Filtro>('todas');

  const estacoes = useMemo(
    () =>
      (rede?.estacoes ?? [])
        .filter((e) => (filtro === 'integracoes' ? e.linhaIds.length > 1 : true))
        .filter((e) => contemTexto(e.nome, busca))
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
    [rede, busca, filtro],
  );

  const abrirEstacao = useCallback(
    (estacao: EstacaoRede) =>
      router.push({ pathname: '/estacoes/[id]', params: { id: String(estacao.id) } }),
    [],
  );

  const contagem = rede ? pluralizar(estacoes.length, 'estação', 'estações') : null;

  return (
    <Screen>
      <View className="gap-3 px-4 pb-3 pt-4">
        <CabecalhoAba titulo="Estações" />
        <SearchInput valor={busca} aoMudar={setBusca} placeholder="Buscar estação pelo nome" />
        <View className="flex-row items-center gap-2">
          {FILTROS.map((f) => (
            <Chip
              key={f.valor}
              rotulo={f.rotulo}
              selecionado={filtro === f.valor}
              onPress={() => setFiltro(f.valor)}
            />
          ))}
          {contagem && (
            <Text className="ml-auto text-xs text-ink-muted" accessibilityLiveRegion="polite">
              {contagem}
            </Text>
          )}
        </View>
      </View>

      {error && !rede ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <FlatList
          data={estacoes}
          keyExtractor={(e) => String(e.id)}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
          contentContainerClassName="px-4 pb-6"
          initialNumToRender={14}
          ListEmptyComponent={
            isPending ? (
              <SkeletonLista itens={8} />
            ) : (
              <EmptyState
                icone={SearchX}
                titulo="Nenhuma estação encontrada"
                mensagem={busca ? 'Confira a grafia ou tente outro nome.' : undefined}
                acao={
                  busca || filtro !== 'todas'
                    ? {
                        rotulo: 'Limpar filtros',
                        onPress: () => {
                          setBusca('');
                          setFiltro('todas');
                        },
                      }
                    : undefined
                }
              />
            )
          }
          renderItem={({ item }) => {
            const linhas = linhasDa(item, index);
            return (
              <EstacaoItem
                estacao={item}
                linhas={linhas}
                comAlerta={linhas.some((l) => !ehStatusNormal(l.status))}
                onPress={abrirEstacao}
              />
            );
          }}
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
