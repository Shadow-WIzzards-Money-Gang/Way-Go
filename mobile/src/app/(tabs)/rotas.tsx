import { router } from 'expo-router';
import { Bookmark, Plus } from 'lucide-react-native';
import { useCallback, useState } from 'react';
import { Alert, FlatList, Pressable, RefreshControl, Switch, Text, View } from 'react-native';

import { ApiError } from '@/api/errors';
import { AvisoBanner } from '@/components/AvisoBanner';
import { CabecalhoAba } from '@/components/CabecalhoAba';
import { ConviteLogin } from '@/components/ConviteLogin';
import { RotaSalvaCard } from '@/components/RotaSalvaCard';
import { Card } from '@/components/ui/Card';
import { Screen } from '@/components/ui/Screen';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useExcluirRota, useRede } from '@/hooks/queries';
import { type RedeIndex, useRedeIndex } from '@/hooks/useRedeIndex';
import { type RotaDoUsuario, useRotasDoUsuario } from '@/hooks/useRotasDoUsuario';
import { cancelarLembretes, pedirPermissaoNotificacoes } from '@/services/notifications';
import { usePlanejamento } from '@/store/planejamento';
import { useRotasLocais } from '@/store/rotasLocais';
import { useLogado, useSessao } from '@/store/sessao';
import { colors } from '@/theme/colors';
import type { StatusOperacional } from '@/types/api';
import { ehStatusNormal, infoStatus, statusDaRota } from '@/utils/status';
import { formatarNomeLinha } from '@/utils/text';

/** Primeira rota salva afetada por alguma linha com problema (banner do topo). */
function encontrarRotaAfetada(rotas: RotaDoUsuario[], index: RedeIndex) {
  for (const rota of rotas) {
    for (const l of rota.linhas) {
      const linha = index.linhaPorId.get(l.id);
      if (linha && !ehStatusNormal(linha.status)) return { rota, linha };
    }
  }
  return null;
}

export default function RotasScreen() {
  const logado = useLogado();
  const sessaoCarregada = useSessao((s) => s.carregada);
  const { rotas, isPending, error, refetch, isRefetching } = useRotasDoUsuario();
  const excluirRota = useExcluirRota();
  const removerLocal = useRotasLocais((s) => s.remover);
  const alertasAtivos = useRotasLocais((s) => s.alertasAtivos);
  const definirAlertas = useRotasLocais((s) => s.definirAlertas);
  const { data: rede } = useRede();
  const index = useRedeIndex(rede);
  const [excluindoId, setExcluindoId] = useState<number | null>(null);

  const statusDe = (rota: RotaDoUsuario) =>
    statusDaRota(
      rota.linhas
        .map((l) => index.linhaPorId.get(l.id)?.status)
        .filter((s): s is StatusOperacional => s !== undefined),
    );

  /** Carrega a rota no planejador e leva ao Mapa. */
  const abrirNoMapa = useCallback((rota: RotaDoUsuario) => {
    const plano = usePlanejamento.getState();
    plano.definirEstacao('origem', { id: rota.origem.id, nome: rota.origem.nome });
    plano.definirEstacao('destino', { id: rota.destino.id, nome: rota.destino.nome });
    plano.definirCriterio(rota.criterio);
    router.navigate('/');
  }, []);

  const novaRota = () => {
    usePlanejamento.getState().limpar();
    router.navigate('/');
  };

  const editar = useCallback(
    (rota: RotaDoUsuario) =>
      router.push({ pathname: '/rota-salva', params: { id: String(rota.id) } }),
    [],
  );
  const agendar = useCallback(
    (rota: RotaDoUsuario) =>
      router.push({ pathname: '/rota-salva', params: { id: String(rota.id), agendar: '1' } }),
    [],
  );

  const { mutate: excluirNaApi } = excluirRota;
  const excluir = useCallback(
    (rota: RotaDoUsuario) =>
      Alert.alert('Excluir rota?', `"${rota.nome}" e seus lembretes serão removidos.`, [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Excluir',
          style: 'destructive',
          onPress: () => {
            setExcluindoId(rota.id);
            excluirNaApi(rota.id, {
              onSuccess: () => {
                removerLocal(rota.id);
                void cancelarLembretes(rota.id).catch(() => undefined);
              },
              onError: (erro) => {
                // 401 já abre o login; os demais erros merecem um aviso.
                if (erro instanceof ApiError && erro.tipo === 'nao-autorizado') return;
                Alert.alert(
                  'Não foi possível excluir',
                  erro instanceof ApiError ? erro.message : 'Tente novamente em instantes.',
                );
              },
              onSettled: () => setExcluindoId(null),
            });
          },
        },
      ]),
    [excluirNaApi, removerLocal],
  );

  const alternarAlertas = async (ativar: boolean) => {
    if (!ativar) {
      definirAlertas(false);
      return;
    }
    const permitido = await pedirPermissaoNotificacoes().catch(() => false);
    if (!permitido) {
      Alert.alert(
        'Permissão necessária',
        'Ative as notificações do WayGo nas configurações do aparelho para receber alertas.',
      );
      return;
    }
    definirAlertas(true);
  };

  const alerta = encontrarRotaAfetada(rotas, index);

  const cabecalho = (
    <View className="gap-4 pb-4">
      <CabecalhoAba
        titulo="Minhas rotas"
        acoes={
          logado ? (
            <Pressable
              onPress={novaRota}
              accessibilityRole="button"
              accessibilityLabel="Nova rota"
              accessibilityHint="Abre o mapa para escolher origem e destino"
              className="min-h-[44px] flex-row items-center gap-1 rounded-full bg-primary-soft px-3.5 active:opacity-80"
            >
              <Plus size={16} color={colors.primary.DEFAULT} />
              <Text className="font-semibold text-primary">Nova</Text>
            </Pressable>
          ) : null
        }
      />
      {alerta && (
        <AvisoBanner
          titulo={`Atenção na sua rota ${alerta.rota.nome}`}
          mensagem={`${formatarNomeLinha(alerta.linha.nome)}: ${
            alerta.linha.statusDescricao ?? infoStatus(alerta.linha.status).rotulo
          }`}
        />
      )}
    </View>
  );

  if (!sessaoCarregada) {
    return (
      <Screen className="px-4 pt-4">
        {cabecalho}
        <SkeletonLista itens={3} className="h-32" />
      </Screen>
    );
  }

  if (!logado) {
    return (
      <Screen className="px-4 pt-4">
        {cabecalho}
        <ConviteLogin
          icone={Bookmark}
          titulo="Entre para ver suas rotas salvas"
          mensagem="Com uma conta, suas rotas ficam guardadas e aparecem em qualquer aparelho."
          parametros={{ motivo: 'rotas' }}
        />
      </Screen>
    );
  }

  if (error && rotas.length === 0) {
    return (
      <Screen className="px-4 pt-4">
        {cabecalho}
        <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
      </Screen>
    );
  }

  const rodape =
    rotas.length > 0 ? (
      <Card className="mt-1 flex-row items-center gap-3">
        <View className="flex-1">
          <Text className="text-base font-semibold text-ink">Alertas das minhas rotas</Text>
          <Text className="text-sm text-ink-muted">
            Aviso no celular quando uma linha das rotas salvas mudar de status.
          </Text>
        </View>
        <Switch
          value={alertasAtivos}
          onValueChange={(v) => void alternarAlertas(v)}
          trackColor={{ true: colors.primary.DEFAULT, false: colors.border }}
          accessibilityLabel="Alertas das minhas rotas"
        />
      </Card>
    ) : undefined;

  return (
    <Screen>
      <FlatList
        data={rotas}
        keyExtractor={(r) => String(r.id)}
        contentContainerClassName="px-4 pt-4 pb-6"
        ListHeaderComponent={cabecalho}
        ListFooterComponent={rodape}
        ListEmptyComponent={
          isPending ? (
            <SkeletonLista itens={3} className="h-32" />
          ) : (
            <EmptyState
              icone={Bookmark}
              titulo="Nenhuma rota salva"
              mensagem="Calcule um trajeto no mapa e toque em “Salvar rota” para acompanhá-lo aqui."
              acao={{ rotulo: 'Planejar rota', onPress: novaRota }}
            />
          )
        }
        renderItem={({ item }) => (
          <RotaSalvaCard
            rota={item}
            status={statusDe(item)}
            excluindo={excluindoId === item.id}
            aoAbrir={abrirNoMapa}
            aoEditar={editar}
            aoAgendar={agendar}
            aoExcluir={excluir}
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
