import * as Haptics from 'expo-haptics';
import { router } from 'expo-router';
import { Focus, RefreshCw } from 'lucide-react-native';
import { useEffect, useMemo, useRef, useState } from 'react';
import { ActivityIndicator, BackHandler, Pressable, Text, View } from 'react-native';
import Animated, { FadeInDown, FadeOutDown } from 'react-native-reanimated';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AvatarConta } from '@/components/AvatarConta';
import { EstacaoMapaCard } from '@/components/EstacaoMapaCard';
import { MapaRede, type MapaRedeHandle } from '@/components/MapaRede';
import { RouteInputCard } from '@/components/RouteInputCard';
import { type EtapaPlanejamento, RotaSugeridaPanel } from '@/components/RotaSugeridaPanel';
import { PainelInferior } from '@/components/ui/PainelInferior';
import { useRede, useRota } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { abrirLogin, hrefComParams } from '@/lib/conta';
import { type CampoEstacao, usePlanejamento } from '@/store/planejamento';
import { useLogado } from '@/store/sessao';
import { colors } from '@/theme/colors';
import type { LinhaRede } from '@/types/api';
import { rotaTemLinhaComProblema } from '@/utils/status';
import { compararCodigos } from '@/utils/text';

const ALTURA_CARD_ROTA = 120;

const SOMBRA_FLUTUANTE = {
  shadowColor: colors.ink.DEFAULT,
  shadowOpacity: 0.12,
  shadowRadius: 12,
  shadowOffset: { width: 0, height: 4 },
  elevation: 5,
};

export default function MapaScreen() {
  const insets = useSafeAreaInsets();
  const [alturaPainel, setAlturaPainel] = useState(200);
  const mapa = useRef<MapaRedeHandle>(null);

  const { origem, destino, criterio, inverter, definirCriterio, definirEstacao } =
    usePlanejamento();
  const logado = useLogado();
  const {
    data: rede,
    isPending: carregandoRede,
    error: erroRede,
    refetch: refetchRede,
  } = useRede();
  const index = useRedeIndex(rede);

  const params = useMemo(
    () => (origem && destino ? { origem: origem.nome, destino: destino.nome, criterio } : null),
    [origem, destino, criterio],
  );
  const rotaQuery = useRota(params);
  const rotaSugerida = params ? rotaQuery.data : undefined;
  const sugeridaTemProblema = rotaSugerida ? rotaTemLinhaComProblema(rotaSugerida) : false;

  // A escolha "evitar linhas com problema" vale só para o trajeto/critério em que foi feita.
  const chaveTrajeto = params ? `${params.origem}|${params.destino}|${params.criterio}` : '';
  const [escolhaAlternativa, setEscolhaAlternativa] = useState({ chave: '', ativa: false });
  const alternativaAtiva =
    sugeridaTemProblema && escolhaAlternativa.chave === chaveTrajeto && escolhaAlternativa.ativa;
  const alternarRota = (ativa: boolean) => setEscolhaAlternativa({ chave: chaveTrajeto, ativa });

  const alternativaQuery = useRota(
    alternativaAtiva && params ? { ...params, alternativa: true } : null,
  );
  const rotaQueryAtual = alternativaAtiva ? alternativaQuery : rotaQuery;
  const rota = alternativaAtiva ? alternativaQuery.data : rotaSugerida;

  // --- Estação tocada no mapa ---
  const [selecionadaId, setSelecionadaId] = useState<number | null>(null);
  const selecionada = selecionadaId != null ? index.estacaoPorId.get(selecionadaId) : undefined;
  const linhasDaSelecionada = (selecionada?.linhaIds ?? [])
    .map((id) => index.linhaPorId.get(id))
    .filter((l): l is LinhaRede => l !== undefined)
    .sort((a, b) => compararCodigos(a.codigo, b.codigo));
  const papelDaSelecionada: CampoEstacao | null =
    selecionada && origem?.id === selecionada.id
      ? 'origem'
      : selecionada && destino?.id === selecionada.id
        ? 'destino'
        : null;

  const tocarEstacao = (id: number) => {
    void Haptics.selectionAsync().catch(() => undefined);
    setSelecionadaId(id);
  };
  const fecharEstacao = () => setSelecionadaId(null);

  // Botão voltar do Android fecha o card antes de sair da tela.
  useEffect(() => {
    if (selecionadaId == null) return;
    const assinatura = BackHandler.addEventListener('hardwareBackPress', () => {
      setSelecionadaId(null);
      return true;
    });
    return () => assinatura.remove();
  }, [selecionadaId]);

  const definirPelaEstacao = (campo: CampoEstacao) => {
    if (!selecionada) return;
    definirEstacao(campo, { id: selecionada.id, nome: selecionada.nome });
    setSelecionadaId(null);
  };

  const verDetalhes = () => {
    if (!selecionada) return;
    router.push({ pathname: '/estacoes/[id]', params: { id: String(selecionada.id) } });
  };

  const abrirLinha = (linhaId: number) =>
    router.push({ pathname: '/linhas/[id]', params: { id: String(linhaId) } });

  const faltando: EtapaPlanejamento =
    !origem && !destino ? 'ambos' : !origem ? 'origem' : 'destino';

  const escolher = (campo: CampoEstacao) =>
    router.push({ pathname: '/selecionar-estacao', params: { campo } });

  /** Salvar exige conta: sem login, passa pelo login e volta direto ao formulário. */
  const abrirRotaSalva = (agendar: boolean) => {
    if (!origem || !destino) return;
    const params = {
      origemId: String(origem.id),
      origemNome: origem.nome,
      destinoId: String(destino.id),
      destinoNome: destino.nome,
      criterio,
      ...(agendar ? { agendar: '1' } : {}),
    };
    if (!logado) {
      abrirLogin({ motivo: 'salvar-rota', proximo: hrefComParams('/rota-salva', params) });
      return;
    }
    router.push({ pathname: '/rota-salva', params });
  };

  return (
    <View className="flex-1 bg-background">
      <MapaRede
        ref={mapa}
        rede={rede}
        index={index}
        rota={rota}
        margens={{ top: insets.top + ALTURA_CARD_ROTA, bottom: alturaPainel }}
        estacaoSelecionadaId={selecionadaId}
        origemId={origem?.id ?? null}
        destinoId={destino?.id ?? null}
        aoTocarEstacao={tocarEstacao}
        aoTocarMapa={fecharEstacao}
      />

      <View className="absolute left-4 right-[72px]" style={{ top: insets.top + 12 }}>
        <RouteInputCard
          origem={origem}
          destino={destino}
          aoEscolher={escolher}
          aoInverter={inverter}
        />
        {(carregandoRede || (erroRede && !rede)) && (
          <View className="mt-2 flex-row">
            {carregandoRede ? (
              <View
                className="flex-row items-center gap-2 rounded-full bg-surface px-3 py-2"
                style={SOMBRA_FLUTUANTE}
                accessibilityLiveRegion="polite"
              >
                <ActivityIndicator size="small" color={colors.primary.DEFAULT} />
                <Text className="text-xs font-medium text-ink-muted">Carregando a rede…</Text>
              </View>
            ) : (
              <Pressable
                onPress={() => void refetchRede()}
                accessibilityRole="button"
                accessibilityLabel="Não foi possível carregar a rede. Tentar novamente"
                className="min-h-[40px] flex-row items-center gap-2 rounded-full bg-surface px-3 active:opacity-80"
                style={SOMBRA_FLUTUANTE}
              >
                <RefreshCw size={14} color={colors.danger.DEFAULT} />
                <Text className="text-xs font-medium text-danger">
                  Rede indisponível · Tentar de novo
                </Text>
              </Pressable>
            )}
          </View>
        )}
      </View>
      <View className="absolute right-4" style={{ top: insets.top + 12 }}>
        <AvatarConta />
      </View>

      {rede && (
        <Pressable
          onPress={() => mapa.current?.enquadrar()}
          accessibilityRole="button"
          accessibilityLabel={rota ? 'Enquadrar a rota' : 'Ver a rede inteira'}
          className="absolute right-4 h-12 w-12 items-center justify-center rounded-full bg-surface active:opacity-80"
          style={[SOMBRA_FLUTUANTE, { bottom: alturaPainel + 12 }]}
        >
          <Focus size={22} color={colors.ink.DEFAULT} />
        </Pressable>
      )}

      <View
        className="absolute bottom-0 left-0 right-0"
        onLayout={(e) => setAlturaPainel(e.nativeEvent.layout.height)}
        pointerEvents="box-none"
      >
        {selecionada ? (
          <Animated.View
            key={`estacao-${selecionada.id}`}
            entering={FadeInDown.duration(220)}
            exiting={FadeOutDown.duration(150)}
          >
            <PainelInferior className="pt-5">
              <EstacaoMapaCard
                estacao={selecionada}
                linhas={linhasDaSelecionada}
                papel={papelDaSelecionada}
                aoDefinir={definirPelaEstacao}
                aoVerDetalhes={verDetalhes}
                aoAbrirLinha={abrirLinha}
                aoFechar={fecharEstacao}
              />
            </PainelInferior>
          </Animated.View>
        ) : (
          <Animated.View key="rota" entering={FadeInDown.duration(220)}>
            <RotaSugeridaPanel
              temSelecao={params !== null}
              faltando={faltando}
              rota={rota}
              carregando={rotaQueryAtual.isFetching}
              erro={params ? rotaQueryAtual.error : null}
              criterio={criterio}
              aoMudarCriterio={definirCriterio}
              mostrarOpcaoAlternativa={sugeridaTemProblema}
              alternativaAtiva={alternativaAtiva}
              aoAlternar={alternarRota}
              aoAbrirLinha={abrirLinha}
              aoTentarNovamente={() => void rotaQueryAtual.refetch()}
              aoAgendar={() => abrirRotaSalva(true)}
              aoSalvar={() => abrirRotaSalva(false)}
            />
          </Animated.View>
        )}
      </View>
    </View>
  );
}
