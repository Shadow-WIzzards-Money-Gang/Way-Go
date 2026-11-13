import { router, Stack } from 'expo-router';
import { ChevronRight, MapPinOff, Plus, SearchX } from 'lucide-react-native';
import { useRef, useState } from 'react';
import { FlatList, Pressable, RefreshControl, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Paginacao } from '@/components/ui/Paginacao';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useEstacoes } from '@/hooks/queries';
import { useValorAtrasado } from '@/hooks/useValorAtrasado';
import { colors } from '@/theme/colors';
import type { EstacaoResumo } from '@/types/api';
import { formatarCoordenada } from '@/utils/admin';
import { pluralizar } from '@/utils/format';

const TAMANHO_PAGINA = 20;

export default function EstacoesAdminScreen() {
  const [busca, setBusca] = useState('');
  const buscaAtrasada = useValorAtrasado(busca.trim());
  // A página pertence à busca em que foi escolhida: mudou a busca, volta para a 1ª.
  const [navegacao, setNavegacao] = useState({ busca: buscaAtrasada, pagina: 0 });
  const pagina = navegacao.busca === buscaAtrasada ? navegacao.pagina : 0;
  const listaRef = useRef<FlatList<EstacaoResumo>>(null);

  const { data, isPending, isPlaceholderData, error, refetch, isRefetching } = useEstacoes({
    pagina,
    tamanho: TAMANHO_PAGINA,
    busca: buscaAtrasada,
  });

  const mudarPagina = (nova: number) => {
    setNavegacao({ busca: buscaAtrasada, pagina: nova });
    listaRef.current?.scrollToOffset({ offset: 0, animated: false });
  };

  const abrir = (estacao: EstacaoResumo) =>
    router.push({ pathname: '/admin/estacoes/[id]', params: { id: String(estacao.id) } });

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
              onPress={() => router.push('/admin/estacoes/nova')}
            />
          ),
        }}
      />
      <View className="gap-2 px-4 pb-2 pt-3">
        <SearchInput valor={busca} aoMudar={setBusca} placeholder="Buscar estação pelo nome" />
        {data && (
          <Text className="text-xs text-ink-muted" accessibilityLiveRegion="polite">
            {pluralizar(data.totalItens, 'estação', 'estações')}
          </Text>
        )}
      </View>

      {error && !data ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <>
          <FlatList
            ref={listaRef}
            data={data?.conteudo ?? []}
            keyExtractor={(e) => String(e.id)}
            keyboardShouldPersistTaps="handled"
            keyboardDismissMode="on-drag"
            className={isPlaceholderData ? 'opacity-60' : undefined}
            contentContainerClassName="px-4 pb-6"
            ListEmptyComponent={
              isPending ? (
                <SkeletonLista itens={8} />
              ) : (
                <EmptyState icone={SearchX} titulo="Nenhuma estação encontrada" />
              )
            }
            renderItem={({ item }) => {
              const semPosicao = item.latitude === null || item.longitude === null;
              return (
                <Pressable
                  onPress={() => abrir(item)}
                  accessibilityRole="button"
                  accessibilityLabel={`${item.nome}${item.ativa ? '' : ', desativada'}${
                    semPosicao ? ', sem posição no mapa' : ''
                  }`}
                  accessibilityHint="Edita a estação"
                  className={`mb-2 flex-row items-center gap-3 rounded-2xl border border-border bg-surface p-3 active:opacity-80 ${
                    item.ativa ? '' : 'opacity-70'
                  }`}
                >
                  <View className="flex-1 gap-1">
                    <Text className="text-base font-medium text-ink" numberOfLines={1}>
                      {item.nome}
                    </Text>
                    <View className="flex-row flex-wrap items-center gap-1.5">
                      {!item.ativa && <Badge rotulo="Desativada" tom="neutral" />}
                      {semPosicao ? (
                        <View className="flex-row items-center gap-1">
                          <MapPinOff size={12} color={colors.warning.DEFAULT} />
                          <Text className="text-xs text-warning">Sem posição</Text>
                        </View>
                      ) : (
                        <Text className="text-xs text-ink-muted">
                          {formatarCoordenada(item.latitude)}, {formatarCoordenada(item.longitude)}
                        </Text>
                      )}
                    </View>
                  </View>
                  <ChevronRight size={18} color={colors.ink.muted} />
                </Pressable>
              );
            }}
            refreshControl={
              <RefreshControl
                refreshing={isRefetching && !isPlaceholderData}
                onRefresh={() => void refetch()}
                tintColor={colors.primary.DEFAULT}
                colors={[colors.primary.DEFAULT]}
              />
            }
          />
          {data && (
            <Paginacao
              pagina={pagina}
              totalPaginas={data.totalPaginas}
              aoMudar={mudarPagina}
              carregando={isPlaceholderData}
            />
          )}
        </>
      )}
    </SafeAreaView>
  );
}
