import { router, Stack, useLocalSearchParams } from 'expo-router';
import { MapPinOff } from 'lucide-react-native';
import { useMemo, useState } from 'react';
import { FlatList, Pressable, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { LinhaBadge } from '@/components/LinhaBadge';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useRede } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { type CampoEstacao, usePlanejamento } from '@/store/planejamento';
import type { EstacaoRede } from '@/types/api';
import { contemTexto } from '@/utils/text';

/**
 * Modal de busca de estação aberto pelo Mapa (campo=origem|destino).
 * O resultado vai para o store de planejamento, que o Mapa observa.
 */
export default function SelecionarEstacaoScreen() {
  const { campo } = useLocalSearchParams<{ campo?: CampoEstacao }>();
  const campoAlvo: CampoEstacao = campo === 'destino' ? 'destino' : 'origem';
  const [busca, setBusca] = useState('');

  const definirEstacao = usePlanejamento((s) => s.definirEstacao);
  const outra = usePlanejamento((s) => (campoAlvo === 'origem' ? s.destino : s.origem));
  const { data: rede, isPending, error, refetch } = useRede();
  const index = useRedeIndex(rede);

  const estacoes = useMemo(
    () =>
      (rede?.estacoes ?? [])
        .filter((e) => e.id !== outra?.id && contemTexto(e.nome, busca))
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
    [rede, busca, outra],
  );

  const selecionar = (estacao: EstacaoRede) => {
    definirEstacao(campoAlvo, { id: estacao.id, nome: estacao.nome });
    router.back();
  };

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <Stack.Screen
        options={{ title: campoAlvo === 'origem' ? 'De onde você sai?' : 'Para onde você vai?' }}
      />
      <View className="px-4 pb-2 pt-4">
        <SearchInput valor={busca} aoMudar={setBusca} placeholder="Buscar estação" autoFocus />
      </View>
      {isPending ? (
        <View className="px-4 pt-2">
          <SkeletonLista itens={8} className="h-12" />
        </View>
      ) : error ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <FlatList
          data={estacoes}
          keyExtractor={(e) => String(e.id)}
          keyboardShouldPersistTaps="handled"
          contentContainerClassName="px-4 pb-6"
          initialNumToRender={20}
          ListEmptyComponent={
            <EmptyState
              icone={MapPinOff}
              titulo="Nenhuma estação encontrada"
              mensagem="Confira a grafia ou tente parte do nome."
            />
          }
          ItemSeparatorComponent={() => <View className="h-px bg-border" />}
          renderItem={({ item }) => (
            <Pressable
              onPress={() => selecionar(item)}
              accessibilityRole="button"
              accessibilityLabel={item.nome}
              className="min-h-[52px] flex-row items-center gap-3 py-3 active:opacity-70"
            >
              <Text className="flex-1 text-base text-ink" numberOfLines={1}>
                {item.nome}
              </Text>
              <View className="flex-row gap-1">
                {item.linhaIds.map((id) => {
                  const linha = index.linhaPorId.get(id);
                  return linha ? (
                    <LinhaBadge key={id} codigo={linha.codigo} cor={linha.cor} tamanho="sm" />
                  ) : null;
                })}
              </View>
            </Pressable>
          )}
        />
      )}
    </SafeAreaView>
  );
}
