import { type Ref, useEffect, useImperativeHandle, useMemo, useRef, useState } from 'react';
import { Platform } from 'react-native';
import MapView, { type MapPressEvent, Polyline } from 'react-native-maps';

import type { RedeIndex } from '@/hooks/useRedeIndex';
import { colors } from '@/theme/colors';
import { ESTILO_MAPA } from '@/theme/estiloMapa';
import type { Rede, Rota } from '@/types/api';
import { corSegura } from '@/utils/color';
import {
  type Coordenada,
  coordenadasDe,
  dentroDaRegiao,
  mostrarEstacao,
  nivelDeZoom,
  paraCoordenada,
  type Regiao,
  regiaoFocadaEm,
} from '@/utils/mapa';

import { type EstadoPonto, PontoEstacao } from './mapa/PontoEstacao';

const REGIAO_SAO_PAULO: Regiao = {
  latitude: -23.5505,
  longitude: -46.6333,
  latitudeDelta: 0.45,
  longitudeDelta: 0.45,
};

export interface MapaRedeHandle {
  /** Enquadra a rota calculada ou, sem rota, a rede inteira. */
  enquadrar: () => void;
}

interface MapaRedeProps {
  ref?: Ref<MapaRedeHandle>;
  rede: Rede | undefined;
  index: RedeIndex;
  rota: Rota | undefined;
  /** Espaço ocupado por elementos sobrepostos, para enquadrar a rota sem escondê-la. */
  margens: { top: number; bottom: number };
  estacaoSelecionadaId: number | null;
  origemId: number | null;
  destinoId: number | null;
  aoTocarEstacao: (id: number) => void;
  /** Toque numa área vazia do mapa (fecha o card da estação). */
  aoTocarMapa: () => void;
}

interface EstacaoNoMapa {
  id: number;
  nome: string;
  coordenada: Coordenada;
  ehIntegracao: boolean;
  corLinha: string | null;
}

/**
 * Mapa da rede no estilo dos mapas de metrô: fundo claro e discreto, linhas com contorno branco
 * e estações tocáveis. Com rota calculada, a rede vira pano de fundo e a rota ganha destaque.
 */
export function MapaRede({
  ref,
  rede,
  index,
  rota,
  margens,
  estacaoSelecionadaId,
  origemId,
  destinoId,
  aoTocarEstacao,
  aoTocarMapa,
}: MapaRedeProps) {
  const mapa = useRef<MapView>(null);
  const regiaoAtual = useRef<Regiao>(REGIAO_SAO_PAULO);
  const [regiao, setRegiao] = useState<Regiao>(REGIAO_SAO_PAULO);
  const nivel = nivelDeZoom(regiao.longitudeDelta);

  const linhas = useMemo(
    () =>
      (rede?.linhas ?? []).map((linha) => ({
        id: linha.id,
        cor: linha.cor,
        pontos: coordenadasDe(
          linha.estacaoIds.map((id) => index.estacaoPorId.get(id)).filter((e) => e !== undefined),
        ),
      })),
    [rede, index],
  );

  const estacoes = useMemo<EstacaoNoMapa[]>(
    () =>
      (rede?.estacoes ?? []).flatMap((e) => {
        const coordenada = paraCoordenada(e);
        if (!coordenada) return [];
        const ehIntegracao = e.linhaIds.length > 1;
        const corLinha = ehIntegracao ? null : (index.linhaPorId.get(e.linhaIds[0])?.cor ?? null);
        return [{ id: e.id, nome: e.nome, coordenada, ehIntegracao, corLinha }];
      }),
    [rede, index],
  );

  const trechos = useMemo(
    () =>
      (rota?.trechos ?? []).map((t) => ({
        ordem: t.ordem,
        cor: t.linha.cor,
        pontos: coordenadasDe(t.estacoes),
      })),
    [rota],
  );

  const idsNaRota = useMemo(
    () => new Set((rota?.trechos ?? []).flatMap((t) => t.estacoes.map((e) => e.id))),
    [rota],
  );
  const temRota = trechos.length > 0;
  const origemDaRota = rota?.origem.id ?? origemId;
  const destinoDaRota = rota?.destino.id ?? destinoId;

  const edgePadding = {
    top: margens.top + 32,
    bottom: margens.bottom + 32,
    left: 48,
    right: 48,
  };

  const enquadrarPontos = (pontos: Coordenada[]) => {
    if (pontos.length < 2) return;
    mapa.current?.fitToCoordinates(pontos, { edgePadding, animated: true });
  };

  useImperativeHandle(ref, () => ({
    enquadrar: () =>
      enquadrarPontos(
        temRota ? trechos.flatMap((t) => t.pontos) : estacoes.map((e) => e.coordenada),
      ),
  }));

  // Nova rota: enquadra o trajeto inteiro. Só quando o trajeto muda de fato: os refetches
  // periódicos da mesma rota não devem puxar o mapa enquanto a pessoa explora.
  const ultimaRotaEnquadrada = useRef('');
  useEffect(() => {
    const pontos = trechos.flatMap((t) => t.pontos);
    if (pontos.length < 2) return;
    const chave = rota
      ? `${rota.origem.id}|${rota.destino.id}|${rota.criterio}|${rota.alternativa}|` +
        rota.trechos.map((t) => t.linha.id).join(',')
      : '';
    if (chave === ultimaRotaEnquadrada.current) return;
    ultimaRotaEnquadrada.current = chave;
    mapa.current?.fitToCoordinates(pontos, {
      edgePadding: { top: margens.top + 32, bottom: margens.bottom + 32, left: 48, right: 48 },
      animated: true,
    });
  }, [rota, trechos, margens.top, margens.bottom]);

  // Estação tocada: centraliza nela (aproximando se estiver longe). Uma vez por seleção, para
  // a atualização periódica do /rede não recentralizar quem já arrastou o mapa.
  const ultimaCentralizada = useRef<number | null>(null);
  useEffect(() => {
    if (estacaoSelecionadaId == null) {
      ultimaCentralizada.current = null;
      return;
    }
    if (ultimaCentralizada.current === estacaoSelecionadaId) return;
    const estacao = estacoes.find((e) => e.id === estacaoSelecionadaId);
    if (!estacao) return;
    ultimaCentralizada.current = estacaoSelecionadaId;
    mapa.current?.animateToRegion(regiaoFocadaEm(estacao.coordenada, regiaoAtual.current), 350);
  }, [estacaoSelecionadaId, estacoes]);

  const estadoDe = (id: number): EstadoPonto => {
    if (id === estacaoSelecionadaId) return 'selecionada';
    if (id === origemDaRota) return 'origem';
    if (id === destinoDaRota) return 'destino';
    if (temRota && !idsNaRota.has(id)) return 'esmaecida';
    return 'padrao';
  };

  const aoPressionarMapa = (e: MapPressEvent) => {
    // No iOS, tocar num marcador também dispara o onPress do mapa.
    if (e.nativeEvent.action === 'marker-press') return;
    aoTocarMapa();
  };

  return (
    <MapView
      ref={mapa}
      style={{ flex: 1 }}
      initialRegion={REGIAO_SAO_PAULO}
      customMapStyle={ESTILO_MAPA}
      mapType={Platform.OS === 'ios' ? 'mutedStandard' : 'standard'}
      showsPointsOfInterests={false}
      showsBuildings={false}
      showsIndoors={false}
      showsTraffic={false}
      showsCompass={false}
      rotateEnabled={false}
      pitchEnabled={false}
      toolbarEnabled={false}
      moveOnMarkerPress={false}
      mapPadding={{ top: margens.top, bottom: margens.bottom, left: 0, right: 0 }}
      onPress={aoPressionarMapa}
      onRegionChangeComplete={(r) => {
        regiaoAtual.current = r;
        setRegiao(r);
      }}
    >
      {/* Rede: contorno branco por baixo deixa as linhas legíveis sobre o mapa. */}
      {!temRota &&
        linhas.map((l) => (
          <Polyline
            key={`contorno-${l.id}`}
            coordinates={l.pontos}
            strokeColor={colors.surface}
            strokeWidth={8}
            lineCap="round"
            lineJoin="round"
            zIndex={1}
          />
        ))}
      {linhas.map((l) => (
        <Polyline
          key={`linha-${l.id}`}
          coordinates={l.pontos}
          // Com rota calculada, a rede vira pano de fundo.
          strokeColor={corSegura(l.cor, temRota ? 0.28 : 1)}
          strokeWidth={temRota ? 3 : 4.5}
          lineCap="round"
          lineJoin="round"
          zIndex={2}
        />
      ))}

      {trechos.map((t) => (
        <Polyline
          key={`contorno-trecho-${t.ordem}`}
          coordinates={t.pontos}
          strokeColor={colors.surface}
          strokeWidth={12}
          lineCap="round"
          lineJoin="round"
          zIndex={8}
        />
      ))}
      {trechos.map((t) => (
        <Polyline
          key={`trecho-${t.ordem}`}
          coordinates={t.pontos}
          strokeColor={corSegura(t.cor)}
          strokeWidth={7}
          lineCap="round"
          lineJoin="round"
          zIndex={9}
        />
      ))}

      {estacoes.map((e) => {
        const estado = estadoDe(e.id);
        const destacada = (estado !== 'padrao' && estado !== 'esmaecida') || idsNaRota.has(e.id);
        if (!mostrarEstacao(nivel, e.ehIntegracao, destacada)) return null;
        const comRotulo =
          (estado !== 'padrao' && estado !== 'esmaecida') ||
          (nivel === 'rua' && dentroDaRegiao(e.coordenada, regiao));
        return (
          <PontoEstacao
            // O visual muda por remontagem: o marcador não acompanha mudanças da View.
            key={`${e.id}-${estado}-${comRotulo ? 'r' : ''}`}
            id={e.id}
            nome={e.nome}
            coordenada={e.coordenada}
            corLinha={e.corLinha}
            estado={estado}
            comRotulo={comRotulo}
            aoTocar={aoTocarEstacao}
          />
        );
      })}
    </MapView>
  );
}
