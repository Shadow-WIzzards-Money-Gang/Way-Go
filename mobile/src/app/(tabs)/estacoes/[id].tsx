import { router, useLocalSearchParams } from 'expo-router';
import { ChevronLeft, ChevronRight, CircleDot, MapPin, MapPinOff, Shuffle } from 'lucide-react-native';
import { Pressable, RefreshControl, ScrollView, Text, View } from 'react-native';

import { AvisoBanner } from '@/components/AvisoBanner';
import { LinhaBadge } from '@/components/LinhaBadge';
import { StatusBadge } from '@/components/StatusBadge';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Screen } from '@/components/ui/Screen';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { ApiError } from '@/api/errors';
import { useEstacao, useRede } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { type CampoEstacao, usePlanejamento } from '@/store/planejamento';
import { colors } from '@/theme/colors';
import type { LinhaDaEstacao, StatusOperacional } from '@/types/api';
import { ehStatusNormal, infoStatus } from '@/utils/status';
import { formatarNomeLinha } from '@/utils/text';

function LinhaDaEstacaoItem({
  linha,
  status,
  ultima,
}: {
  linha: LinhaDaEstacao;
  status: StatusOperacional | undefined;
  ultima: boolean;
}) {
  const nome = formatarNomeLinha(linha.nome);
  return (
    <Pressable
      onPress={() => router.push({ pathname: '/linhas/[id]', params: { id: String(linha.id) } })}
      accessibilityRole="button"
      accessibilityLabel={`${nome}${status ? `, ${infoStatus(status).rotulo}` : ''}`}
      accessibilityHint="Abre as estações e o status da linha"
      className={`min-h-[56px] flex-row items-center gap-3 py-3 active:opacity-70 ${
        ultima ? '' : 'border-b border-border'
      }`}
    >
      <LinhaBadge codigo={linha.codigo} cor={linha.cor} />
      <Text className="flex-1 text-base font-medium text-ink" numberOfLines={1}>
        {nome}
      </Text>
      {status && <StatusBadge status={status} />}
      <ChevronRight size={18} color={colors.ink.subtle} />
    </Pressable>
  );
}

export default function EstacaoScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const estacaoId = Number(id);

  const { data: estacao, isPending, error, refetch, isRefetching } = useEstacao(estacaoId);
  const rede = useRede();
  const index = useRedeIndex(rede.data);

  const voltar = () => (router.canGoBack() ? router.back() : router.replace('/estacoes'));

  /** Leva a estação para o planejador e abre o Mapa, que já calcula a rota. */
  const usarNoMapa = (campo: CampoEstacao) => {
    if (!estacao) return;
    usePlanejamento.getState().definirEstacao(campo, { id: estacao.id, nome: estacao.nome });
    router.navigate('/');
  };

  const linhas = [...(estacao?.linhas ?? [])].sort((a, b) =>
    a.codigo.localeCompare(b.codigo, 'pt-BR', { numeric: true }),
  );
  const statusDe = (linhaId: number) => index.linhaPorId.get(linhaId)?.status;
  const comProblema = linhas
    .map((l) => ({ linha: l, rede: index.linhaPorId.get(l.id) }))
    .filter(({ rede: r }) => r && !ehStatusNormal(r.status));

  const naoEncontrada = error instanceof ApiError && error.tipo === 'nao-encontrado';

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
        {estacao ? (
          <Text
            className="flex-1 text-xl font-bold text-ink"
            numberOfLines={1}
            accessibilityRole="header"
          >
            {estacao.nome}
          </Text>
        ) : (
          <Skeleton className="mr-4 h-7 flex-1" />
        )}
      </View>

      {naoEncontrada ? (
        <EmptyState
          icone={MapPinOff}
          titulo="Estação não encontrada"
          mensagem="Ela pode ter sido removida da rede."
          acao={{ rotulo: 'Ver todas as estações', onPress: voltar }}
        />
      ) : error && !estacao ? (
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      ) : (
        <ScrollView
          contentContainerClassName="gap-4 px-4 pb-8 pt-2"
          refreshControl={
            <RefreshControl
              refreshing={isRefetching}
              onRefresh={() => {
                void refetch();
                void rede.refetch();
              }}
              tintColor={colors.primary.DEFAULT}
              colors={[colors.primary.DEFAULT]}
            />
          }
        >
          {isPending || !estacao ? (
            <>
              <Skeleton className="h-28" />
              <Skeleton className="h-40" />
              <Skeleton className="h-12" />
            </>
          ) : (
            <>
              {!estacao.ativa && (
                <View className="flex-row">
                  <Badge rotulo="Fora de operação" tom="danger" />
                </View>
              )}

              {comProblema.map(({ linha, rede: r }) =>
                r ? (
                  <AvisoBanner
                    key={linha.id}
                    titulo={formatarNomeLinha(linha.nome)}
                    mensagem={r.statusDescricao ?? infoStatus(r.status).rotulo}
                  />
                ) : null,
              )}

              <Card className="py-1">
                <Text className="pt-3 text-xs font-semibold uppercase tracking-wider text-ink-muted">
                  {linhas.length === 1 ? 'Linha nesta estação' : 'Linhas nesta estação'}
                </Text>
                {linhas.length === 0 ? (
                  <Text className="py-3 text-sm text-ink-muted">
                    Nenhuma linha em operação atende esta estação.
                  </Text>
                ) : (
                  linhas.map((l, i) => (
                    <LinhaDaEstacaoItem
                      key={l.id}
                      linha={l}
                      status={statusDe(l.id)}
                      ultima={i === linhas.length - 1}
                    />
                  ))
                )}
              </Card>

              {linhas.length > 1 && (
                <Card className="flex-row items-center gap-3">
                  <View className="h-10 w-10 items-center justify-center rounded-full bg-primary-soft">
                    <Shuffle size={18} color={colors.primary.DEFAULT} />
                  </View>
                  <View className="flex-1">
                    <Text className="text-base font-semibold text-ink">Estação de integração</Text>
                    <Text className="text-sm text-ink-muted">
                      Baldeação entre {linhas.map((l) => `L${l.codigo}`).join(' · ')}
                    </Text>
                  </View>
                </Card>
              )}

              <View className="gap-3">
                <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
                  Planejar viagem
                </Text>
                <View className="flex-row gap-3">
                  <Button
                    rotulo="Sair daqui"
                    icone={CircleDot}
                    variante="outline"
                    onPress={() => usarNoMapa('origem')}
                    accessibilityHint="Define esta estação como origem e abre o mapa"
                    className="flex-1"
                  />
                  <Button
                    rotulo="Ir para cá"
                    icone={MapPin}
                    onPress={() => usarNoMapa('destino')}
                    accessibilityHint="Define esta estação como destino e abre o mapa"
                    className="flex-1"
                  />
                </View>
              </View>
            </>
          )}
        </ScrollView>
      )}
    </Screen>
  );
}
