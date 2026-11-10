import { useEffect, useMemo, useRef } from 'react';
import { View } from 'react-native';
import MapView, { type LatLng, Marker, Polyline } from 'react-native-maps';

import type { RedeIndex } from '@/hooks/useRedeIndex';
import { colors } from '@/theme/colors';
import type { EstacaoResumo, Rede, Rota } from '@/types/api';
import { corSegura } from '@/utils/color';

const REGIAO_SAO_PAULO = {
  latitude: -23.5505,
  longitude: -46.6333,
  latitudeDelta: 0.45,
  longitudeDelta: 0.45,
};

interface MapaRedeProps {
  rede: Rede | undefined;
  index: RedeIndex;
  rota: Rota | undefined;
  /** Espaço ocupado por elementos sobrepostos, para enquadrar a rota sem escondê-la. */
  margens: { top: number; bottom: number };
}

function paraCoordenada(e: { latitude: number | null; longitude: number | null }): LatLng | null {
  return e.latitude == null || e.longitude == null
    ? null
    : { latitude: Number(e.latitude), longitude: Number(e.longitude) };
}

const coordenadas = (estacoes: { latitude: number | null; longitude: number | null }[]) =>
  estacoes.map(paraCoordenada).filter((c): c is LatLng => c !== null);

function Ponto({
  estacao,
  cor,
  grande = false,
}: {
  estacao: EstacaoResumo;
  cor: string;
  grande?: boolean;
}) {
  const coord = paraCoordenada(estacao);
  if (!coord) return null;
  return (
    <Marker
      coordinate={coord}
      title={estacao.nome}
      anchor={{ x: 0.5, y: 0.5 }}
      // Obrigatório no Android (nova arquitetura): sem pinColor o react-native-maps chama
      // Color.colorToHSV(null) e o app quebra com "Integer.intValue() on a null object".
      pinColor={corSegura(cor)}
    >
      <View
        className={`rounded-full border-white ${grande ? 'h-5 w-5 border-[3px]' : 'h-3 w-3 border-2'}`}
        style={{ backgroundColor: cor }}
      />
    </Marker>
  );
}

/** Mapa da rede: todas as linhas em segundo plano e a rota calculada em destaque. */
export function MapaRede({ rede, index, rota, margens }: MapaRedeProps) {
  const mapa = useRef<MapView>(null);

  const linhas = useMemo(
    () =>
      (rede?.linhas ?? []).map((linha) => ({
        id: linha.id,
        cor: linha.cor,
        pontos: coordenadas(
          linha.estacaoIds.map((id) => index.estacaoPorId.get(id)).filter((e) => e !== undefined),
        ),
      })),
    [rede, index],
  );

  const trechos = useMemo(
    () =>
      (rota?.trechos ?? []).map((t) => ({
        ordem: t.ordem,
        cor: t.linha.cor,
        pontos: coordenadas(t.estacoes),
      })),
    [rota],
  );

  useEffect(() => {
    const pontos = trechos.flatMap((t) => t.pontos);
    if (pontos.length < 2) return;
    mapa.current?.fitToCoordinates(pontos, {
      edgePadding: { top: margens.top + 24, bottom: margens.bottom + 24, left: 40, right: 40 },
      animated: true,
    });
  }, [trechos, margens.top, margens.bottom]);

  const temRota = trechos.length > 0;

  return (
    <MapView
      ref={mapa}
      style={{ flex: 1 }}
      initialRegion={REGIAO_SAO_PAULO}
      showsPointsOfInterests={false}
      showsBuildings={false}
      toolbarEnabled={false}
      mapPadding={{ top: margens.top, bottom: margens.bottom, left: 0, right: 0 }}
    >
      {linhas.map((l) => (
        <Polyline
          key={`linha-${l.id}`}
          coordinates={l.pontos}
          // Com rota calculada, a rede vira pano de fundo (cor com transparência).
          strokeColor={corSegura(l.cor, temRota ? 0.33 : 1)}
          strokeWidth={temRota ? 3 : 4}
          lineCap="round"
          lineJoin="round"
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
          zIndex={10}
        />
      ))}
      {rota?.baldeacoes.map((b, i) => (
        <Ponto key={`baldeacao-${i}`} estacao={b.estacaoDestino} cor={b.linhaDestino.cor} />
      ))}
      {rota && <Ponto estacao={rota.origem} cor={colors.success.DEFAULT} grande />}
      {rota && <Ponto estacao={rota.destino} cor={colors.danger.DEFAULT} grande />}
    </MapView>
  );
}
