import { memo, useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { Marker } from 'react-native-maps';

import { colors } from '@/theme/colors';
import { corSegura } from '@/utils/color';
import type { Coordenada } from '@/utils/mapa';

/**
 * - padrao: ponto branco com borda na cor da linha (ou escura, se for integração).
 * - esmaecida: com rota na tela, as estações fora dela ficam em segundo plano.
 * - selecionada: a estação tocada, com anel azul.
 * - origem / destino: preenchidas de verde / vermelho, como no card de origem e destino.
 */
export type EstadoPonto = 'padrao' | 'esmaecida' | 'selecionada' | 'origem' | 'destino';

interface PontoEstacaoProps {
  id: number;
  nome: string;
  coordenada: Coordenada;
  /** Cor da linha (estação de uma linha só) ou null para integração. */
  corLinha: string | null;
  estado: EstadoPonto;
  comRotulo: boolean;
  aoTocar: (id: number) => void;
}

const Z_INDEX: Record<EstadoPonto, number> = {
  esmaecida: 1,
  padrao: 2,
  origem: 4,
  destino: 4,
  selecionada: 5,
};

const ALTURA_ROTULO = 18;

function Bolinha({ corLinha, estado }: { corLinha: string | null; estado: EstadoPonto }) {
  if (estado === 'selecionada') {
    return (
      <View className="h-9 w-9 items-center justify-center rounded-full border border-primary bg-primary-soft">
        <View className="h-5 w-5 rounded-full border-[4px] border-primary bg-surface" />
      </View>
    );
  }
  if (estado === 'origem' || estado === 'destino') {
    const cor = estado === 'origem' ? colors.success.DEFAULT : colors.danger.DEFAULT;
    return (
      <View
        className="h-5 w-5 rounded-full border-[3px] border-surface"
        style={{ backgroundColor: cor, elevation: 3 }}
      />
    );
  }
  const integracao = corLinha === null;
  return (
    <View
      className={`rounded-full bg-surface ${integracao ? 'h-4 w-4 border-[3px]' : 'h-3 w-3 border-[2.5px]'}`}
      style={{ borderColor: integracao ? colors.ink.DEFAULT : corSegura(corLinha) }}
    />
  );
}

/** Marcador de estação no mapa. Toque abre o card da estação (sem o balão padrão do mapa). */
export const PontoEstacao = memo(function PontoEstacao({
  id,
  nome,
  coordenada,
  corLinha,
  estado,
  comRotulo,
  aoTocar,
}: PontoEstacaoProps) {
  // Marcadores com View própria são "fotografados" pelo mapa. Deixar tracksViewChanges ligado
  // pesa com centenas de estações; desligar logo de cara às vezes fotografa antes do layout
  // (marcador em branco no Android). Por isso: liga, espera o layout e desliga.
  const [rastreando, setRastreando] = useState(true);
  useEffect(() => {
    const timer = setTimeout(() => setRastreando(false), 400);
    return () => clearTimeout(timer);
  }, []);

  const destacado = estado !== 'padrao' && estado !== 'esmaecida';

  return (
    <Marker
      identifier={String(id)}
      coordinate={coordenada}
      anchor={{ x: 0.5, y: 0.5 }}
      onPress={() => aoTocar(id)}
      stopPropagation
      tracksViewChanges={rastreando}
      opacity={estado === 'esmaecida' ? 0.45 : 1}
      zIndex={Z_INDEX[estado]}
      accessibilityLabel={`Estação ${nome}`}
      // Obrigatório no Android (nova arquitetura): sem pinColor o react-native-maps chama
      // Color.colorToHSV(null) e o app quebra com "Integer.intValue() on a null object".
      pinColor={corSegura(corLinha)}
    >
      {/* Espaços iguais acima e abaixo da bolinha mantêm o anchor (0.5, 0.5) no centro dela. */}
      <View className="items-center">
        {comRotulo && <View style={{ height: ALTURA_ROTULO }} />}
        <Bolinha corLinha={corLinha} estado={estado} />
        {comRotulo && (
          <View style={{ height: ALTURA_ROTULO }} className="justify-center">
            <Text
              numberOfLines={1}
              className={`max-w-[140px] text-[11px] ${destacado ? 'font-bold text-ink' : 'font-semibold text-ink-muted'}`}
              style={{
                textShadowColor: colors.surface,
                textShadowRadius: 3,
                textShadowOffset: { width: 0, height: 0 },
              }}
            >
              {nome}
            </Text>
          </View>
        )}
      </View>
    </Marker>
  );
});
