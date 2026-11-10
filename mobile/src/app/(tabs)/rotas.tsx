import { router } from 'expo-router';
import { Bookmark, Plus } from 'lucide-react-native';
import { useCallback } from 'react';
import { Alert, FlatList, Pressable, Switch, Text, View } from 'react-native';

import { RotaSalvaCard } from '@/components/RotaSalvaCard';
import { Card } from '@/components/ui/Card';
import { Screen } from '@/components/ui/Screen';
import { EmptyState } from '@/components/ui/States';
import { useRede } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { cancelarLembretes, pedirPermissaoNotificacoes } from '@/services/notifications';
import { usePlanejamento } from '@/store/planejamento';
import { type RotaSalva, useRotasSalvas } from '@/store/rotasSalvas';
import { colors } from '@/theme/colors';
import type { StatusOperacional } from '@/types/api';
import { statusDaRota } from '@/utils/status';

export default function RotasScreen() {
  const rotas = useRotasSalvas((s) => s.rotas);
  const remover = useRotasSalvas((s) => s.remover);
  const alertasAtivos = useRotasSalvas((s) => s.alertasAtivos);
  const definirAlertas = useRotasSalvas((s) => s.definirAlertas);
  const { data: rede } = useRede();
  const index = useRedeIndex(rede);

  const statusDe = (rota: RotaSalva) =>
    statusDaRota(
      rota.linhas
        .map((l) => index.linhaPorId.get(l.id)?.status)
        .filter((s): s is StatusOperacional => s !== undefined),
    );

  /** Carrega a rota no planejador e leva ao Mapa. */
  const abrirNoMapa = useCallback((rota: RotaSalva) => {
    const plano = usePlanejamento.getState();
    plano.definirEstacao('origem', rota.origem);
    plano.definirEstacao('destino', rota.destino);
    plano.definirCriterio(rota.criterio);
    router.navigate('/');
  }, []);

  const novaRota = () => {
    usePlanejamento.getState().limpar();
    router.navigate('/');
  };

  const editar = useCallback(
    (rota: RotaSalva) => router.push({ pathname: '/rota-salva', params: { id: rota.id } }),
    [],
  );
  const agendar = useCallback(
    (rota: RotaSalva) =>
      router.push({ pathname: '/rota-salva', params: { id: rota.id, agendar: '1' } }),
    [],
  );
  const excluir = useCallback(
    (rota: RotaSalva) =>
      Alert.alert('Excluir rota?', `"${rota.nome}" e seus lembretes serão removidos.`, [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Excluir',
          style: 'destructive',
          onPress: () => {
            remover(rota.id);
            void cancelarLembretes(rota.id).catch(() => undefined);
          },
        },
      ]),
    [remover],
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

  const cabecalho = (
    <View className="flex-row items-center justify-between pb-4">
      <Text className="text-3xl font-bold text-ink" accessibilityRole="header">
        Minhas rotas
      </Text>
      <Pressable
        onPress={novaRota}
        accessibilityRole="button"
        accessibilityLabel="Nova rota"
        accessibilityHint="Abre o mapa para escolher origem e destino"
        className="min-h-[40px] flex-row items-center gap-1 rounded-full bg-primary-soft px-3.5 active:opacity-80"
      >
        <Plus size={16} color={colors.primary.DEFAULT} />
        <Text className="font-semibold text-primary">Nova rota</Text>
      </Pressable>
    </View>
  );

  const rodape = (
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
  );

  return (
    <Screen>
      <FlatList
        data={rotas}
        keyExtractor={(r) => r.id}
        contentContainerClassName="px-4 pt-4 pb-6"
        ListHeaderComponent={cabecalho}
        ListFooterComponent={rodape}
        ListEmptyComponent={
          <EmptyState
            icone={Bookmark}
            titulo="Nenhuma rota salva"
            mensagem="Calcule um trajeto no mapa e toque em “Salvar rota” para acompanhá-lo aqui."
            acao={{ rotulo: 'Planejar rota', onPress: novaRota }}
          />
        }
        renderItem={({ item }) => (
          <RotaSalvaCard
            rota={item}
            status={statusDe(item)}
            aoAbrir={abrirNoMapa}
            aoEditar={editar}
            aoAgendar={agendar}
            aoExcluir={excluir}
          />
        )}
      />
    </Screen>
  );
}
