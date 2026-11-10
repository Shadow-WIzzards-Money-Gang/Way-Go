import { ArrowUpDown } from 'lucide-react-native';
import { Pressable, Text, View } from 'react-native';

import type { CampoEstacao, EstacaoSelecionada } from '@/store/planejamento';
import { colors } from '@/theme/colors';

interface RouteInputCardProps {
  origem: EstacaoSelecionada | null;
  destino: EstacaoSelecionada | null;
  aoEscolher: (campo: CampoEstacao) => void;
  aoInverter: () => void;
}

function Campo({
  rotulo,
  valor,
  corPonto,
  onPress,
}: {
  rotulo: string;
  valor: string | undefined;
  corPonto: string;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={`${rotulo}: ${valor ?? 'não definida'}`}
      accessibilityHint="Abre a busca de estações"
      className="min-h-[52px] flex-row items-center gap-3 py-2 active:opacity-70"
    >
      <View className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: corPonto }} />
      <View className="flex-1">
        <Text className="text-xs text-ink-muted">{rotulo}</Text>
        <Text
          className={`text-base font-semibold ${valor ? 'text-ink' : 'text-ink-subtle'}`}
          numberOfLines={1}
        >
          {valor ? `Estação ${valor}` : 'Escolha uma estação'}
        </Text>
      </View>
    </Pressable>
  );
}

/** Card flutuante "Origem / Destino" do topo do mapa. */
export function RouteInputCard({ origem, destino, aoEscolher, aoInverter }: RouteInputCardProps) {
  return (
    <View
      className="flex-row items-center rounded-2xl bg-surface px-4 py-1"
      style={{
        shadowColor: '#0F172A',
        shadowOpacity: 0.12,
        shadowRadius: 16,
        shadowOffset: { width: 0, height: 6 },
        elevation: 6,
      }}
    >
      <View className="flex-1">
        <Campo
          rotulo="Origem"
          valor={origem?.nome}
          corPonto={colors.success.DEFAULT}
          onPress={() => aoEscolher('origem')}
        />
        <View className="ml-5 h-px bg-border" />
        <Campo
          rotulo="Destino"
          valor={destino?.nome}
          corPonto={colors.danger.DEFAULT}
          onPress={() => aoEscolher('destino')}
        />
      </View>
      <Pressable
        onPress={aoInverter}
        disabled={!origem && !destino}
        hitSlop={8}
        accessibilityRole="button"
        accessibilityLabel="Inverter origem e destino"
        className="ml-2 h-11 w-11 items-center justify-center rounded-full active:bg-neutral-soft"
      >
        <ArrowUpDown size={20} color={colors.ink.muted} />
      </Pressable>
    </View>
  );
}
