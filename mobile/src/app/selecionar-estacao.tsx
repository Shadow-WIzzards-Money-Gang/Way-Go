import { router, Stack, useLocalSearchParams, useNavigation } from 'expo-router';
import { Check, MapPinOff } from 'lucide-react-native';
import { memo, useDeferredValue, useEffect, useMemo, useRef, useState } from 'react';
import { FlatList, Platform, Pressable, Text, type TextInputInstance, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { LinhaBadge } from '@/components/LinhaBadge';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useRede } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { type CampoEstacao, usePlanejamento } from '@/store/planejamento';
import { colors } from '@/theme/colors';
import type { EstacaoRede, LinhaRede } from '@/types/api';
import { compararCodigos, compararNomes, normalizar } from '@/utils/text';

interface OpcaoEstacao {
  estacao: EstacaoRede;
  /** Nome sem acentos/caixa, calculado uma vez (a busca roda a cada tecla). */
  nomeNormalizado: string;
  linhas: LinhaRede[];
}

const ItemEstacao = memo(function ItemEstacao({
  opcao,
  atual,
  aoSelecionar,
}: {
  opcao: OpcaoEstacao;
  atual: boolean;
  aoSelecionar: (estacao: EstacaoRede) => void;
}) {
  const { estacao, linhas } = opcao;
  return (
    <Pressable
      onPress={() => aoSelecionar(estacao)}
      accessibilityRole="button"
      accessibilityLabel={`${estacao.nome}${atual ? ', selecionada' : ''}`}
      accessibilityState={{ selected: atual }}
      // Toque registra no primeiro contato, mesmo com o teclado aberto.
      unstable_pressDelay={0}
      className="min-h-[52px] flex-row items-center gap-3 py-3 active:opacity-60"
    >
      <Text
        className={`flex-1 text-base ${atual ? 'font-semibold text-primary' : 'text-ink'}`}
        numberOfLines={1}
      >
        {estacao.nome}
      </Text>
      <View className="flex-row gap-1">
        {linhas.map((l) => (
          <LinhaBadge key={l.id} codigo={l.codigo} cor={l.cor} tamanho="sm" />
        ))}
      </View>
      {atual && <Check size={18} color={colors.primary.DEFAULT} />}
    </Pressable>
  );
});

const Separador = () => <View className="h-px bg-border" />;

/**
 * Modal de busca de estação aberto pelo Mapa (campo=origem|destino).
 * O resultado vai para o store de planejamento, que o Mapa observa.
 */
export default function SelecionarEstacaoScreen() {
  const { campo } = useLocalSearchParams<{ campo?: CampoEstacao }>();
  const campoAlvo: CampoEstacao = campo === 'destino' ? 'destino' : 'origem';
  const [busca, setBusca] = useState('');
  // A lista filtra com o valor "adiado": o campo de texto responde na hora e a lista acompanha.
  const buscaAdiada = useDeferredValue(busca);

  const definirEstacao = usePlanejamento((s) => s.definirEstacao);
  const atualId = usePlanejamento((s) => s[campoAlvo]?.id);
  const outraId = usePlanejamento((s) => (campoAlvo === 'origem' ? s.destino : s.origem)?.id);
  const { data: rede, isPending, error, refetch } = useRede();
  const index = useRedeIndex(rede);

  // Ordenação, normalização e linhas de cada estação: uma vez por carga da rede, não por tecla.
  const opcoes = useMemo<OpcaoEstacao[]>(
    () =>
      (rede?.estacoes ?? [])
        .map((estacao) => ({
          estacao,
          nomeNormalizado: normalizar(estacao.nome),
          linhas: estacao.linhaIds
            .map((id) => index.linhaPorId.get(id))
            .filter((l): l is LinhaRede => l !== undefined)
            .sort((a, b) => compararCodigos(a.codigo, b.codigo)),
        }))
        .sort((a, b) => compararNomes(a.estacao.nome, b.estacao.nome)),
    [rede, index],
  );

  const termo = normalizar(buscaAdiada);
  const visiveis = opcoes.filter(
    (o) => o.estacao.id !== outraId && (termo === '' || o.nomeNormalizado.includes(termo)),
  );

  // Um toque fecha o modal; toques repetidos enquanto ele fecha não podem voltar duas telas.
  const selecionou = useRef(false);
  const selecionar = (estacao: EstacaoRede) => {
    if (selecionou.current) return;
    selecionou.current = true;
    definirEstacao(campoAlvo, { id: estacao.id, nome: estacao.nome });
    router.back();
  };

  // Abrir o teclado só depois da animação do modal: com autoFocus ele abre no meio da
  // transição e deixa a abertura travada (principalmente no Android).
  const campoBusca = useRef<TextInputInstance>(null);
  const navigation = useNavigation();
  useEffect(() => {
    const focar = () => campoBusca.current?.focus();
    const reserva = setTimeout(focar, Platform.OS === 'android' ? 400 : 550);
    const remover = navigation.addListener(
      'transitionEnd' as never,
      (e: { data?: { closing?: boolean } }) => {
        if (e.data?.closing) return;
        clearTimeout(reserva);
        focar();
      },
    );
    return () => {
      clearTimeout(reserva);
      remover();
    };
  }, [navigation]);

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <Stack.Screen
        options={{ title: campoAlvo === 'origem' ? 'De onde você sai?' : 'Para onde você vai?' }}
      />
      <View className="px-4 pb-2 pt-4">
        <SearchInput
          ref={campoBusca}
          valor={busca}
          aoMudar={setBusca}
          placeholder="Buscar estação"
        />
      </View>
      {isPending ? (
        <View className="px-4 pt-2">
          <SkeletonLista itens={8} className="h-12" />
        </View>
      ) : error ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <FlatList
          data={visiveis}
          keyExtractor={(o) => String(o.estacao.id)}
          extraData={atualId}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
          contentContainerClassName="px-4 pb-6"
          initialNumToRender={16}
          maxToRenderPerBatch={16}
          windowSize={7}
          removeClippedSubviews={Platform.OS === 'android'}
          ListEmptyComponent={
            <EmptyState
              icone={MapPinOff}
              titulo="Nenhuma estação encontrada"
              mensagem="Confira a grafia ou tente parte do nome."
            />
          }
          ItemSeparatorComponent={Separador}
          renderItem={({ item }) => (
            <ItemEstacao
              opcao={item}
              atual={item.estacao.id === atualId}
              aoSelecionar={selecionar}
            />
          )}
        />
      )}
    </SafeAreaView>
  );
}
