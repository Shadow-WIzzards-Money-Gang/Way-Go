import { router } from 'expo-router';
import { SearchX } from 'lucide-react-native';
import { useCallback, useRef, useState } from 'react';
import { FlatList, RefreshControl, Text, View } from 'react-native';

import { CabecalhoAba } from '@/components/CabecalhoAba';
import { type EstacaoBasica, EstacaoItem } from '@/components/EstacaoItem';
import { Chip } from '@/components/ui/Chip';
import { Paginacao } from '@/components/ui/Paginacao';
import { Screen } from '@/components/ui/Screen';
import { SearchInput } from '@/components/ui/SearchInput';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useEstacoes, useRede } from '@/hooks/queries';
import { type RedeIndex, useRedeIndex } from '@/hooks/useRedeIndex';
import { useValorAtrasado } from '@/hooks/useValorAtrasado';
import { colors } from '@/theme/colors';
import type { EstacaoResumo, LinhaRede } from '@/types/api';
import { pluralizar } from '@/utils/format';
import { ehStatusNormal } from '@/utils/status';
import { compararCodigos } from '@/utils/text';

type Filtro = 'todas' | 'integracoes';

const FILTROS: { valor: Filtro; rotulo: string }[] = [
  { valor: 'todas', rotulo: 'Todas' },
  { valor: 'integracoes', rotulo: 'Com integração' },
];

const TAMANHO_PAGINA = 20;

/** A lista vem paginada de /estacoes; as linhas de cada estação vêm do índice de /rede. */
function linhasDa(estacaoId: number, index: RedeIndex): LinhaRede[] {
  return (index.estacaoPorId.get(estacaoId)?.linhaIds ?? [])
    .map((id) => index.linhaPorId.get(id))
    .filter((l): l is LinhaRede => l !== undefined)
    .sort((a, b) => compararCodigos(a.codigo, b.codigo));
}

export default function EstacoesScreen() {
  const { data: rede, refetch: refetchRede } = useRede();
  const index = useRedeIndex(rede);
  const [busca, setBusca] = useState('');
  const buscaAtrasada = useValorAtrasado(busca.trim());
  const [filtro, setFiltro] = useState<Filtro>('todas');
  // A página fica atrelada à busca/filtro em que foi escolhida: mudou um deles, volta para a 1ª.
  const chaveFiltros = `${filtro}|${buscaAtrasada}`;
  const [navegacao, setNavegacao] = useState({ chave: chaveFiltros, pagina: 0 });
  const pagina = navegacao.chave === chaveFiltros ? navegacao.pagina : 0;
  const listaRef = useRef<FlatList<EstacaoResumo>>(null);

  const { data, isPending, isPlaceholderData, error, refetch, isRefetching } = useEstacoes({
    pagina,
    tamanho: TAMANHO_PAGINA,
    busca: buscaAtrasada,
    integracoes: filtro === 'integracoes',
  });

  // Se a lista encolheu (ex.: dados mudaram no servidor), não fica preso numa página que sumiu.
  // Ajuste durante o render (padrão do React para estado derivado), sem useEffect.
  if (data && !isPlaceholderData && data.totalPaginas > 0 && pagina >= data.totalPaginas) {
    setNavegacao({ chave: chaveFiltros, pagina: data.totalPaginas - 1 });
  }

  const mudarPagina = useCallback(
    (nova: number) => {
      setNavegacao({ chave: chaveFiltros, pagina: nova });
      listaRef.current?.scrollToOffset({ offset: 0, animated: false });
    },
    [chaveFiltros],
  );

  const abrirEstacao = useCallback(
    (estacao: EstacaoBasica) =>
      router.push({ pathname: '/estacoes/[id]', params: { id: String(estacao.id) } }),
    [],
  );

  const limparFiltros = () => {
    setBusca('');
    setFiltro('todas');
  };

  const contagem = data ? pluralizar(data.totalItens, 'estação', 'estações') : null;
  const filtrando = busca !== '' || filtro !== 'todas';

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
            initialNumToRender={TAMANHO_PAGINA}
            ListEmptyComponent={
              isPending ? (
                <SkeletonLista itens={8} />
              ) : (
                <EmptyState
                  icone={SearchX}
                  titulo="Nenhuma estação encontrada"
                  mensagem={busca ? 'Confira a grafia ou tente outro nome.' : undefined}
                  acao={
                    filtrando ? { rotulo: 'Limpar filtros', onPress: limparFiltros } : undefined
                  }
                />
              )
            }
            renderItem={({ item }) => {
              const linhas = linhasDa(item.id, index);
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
                refreshing={isRefetching && !isPlaceholderData}
                onRefresh={() => {
                  void refetch();
                  void refetchRede();
                }}
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
    </Screen>
  );
}
