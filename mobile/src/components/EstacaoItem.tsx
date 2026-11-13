import { ChevronRight, TriangleAlert } from 'lucide-react-native';
import { memo } from 'react';
import { Pressable, Text, View } from 'react-native';

import { colors } from '@/theme/colors';
import type { LinhaRede } from '@/types/api';

import { LinhaBadge } from './LinhaBadge';

/** Serve tanto para EstacaoRede (/rede) quanto EstacaoResumo (/estacoes). */
export interface EstacaoBasica {
  id: number;
  nome: string;
}

interface EstacaoItemProps {
  estacao: EstacaoBasica;
  /** Linhas que passam pela estação, já ordenadas pelo código. */
  linhas: LinhaRede[];
  /** Alguma linha da estação está fora do normal. */
  comAlerta: boolean;
  onPress: (estacao: EstacaoBasica) => void;
}

/** Card da lista de estações: nome, linhas que atendem e alerta de status. */
export const EstacaoItem = memo(function EstacaoItem({
  estacao,
  linhas,
  comAlerta,
  onPress,
}: EstacaoItemProps) {
  const codigos = linhas.map((l) => `linha ${l.codigo}`).join(', ');
  return (
    <Pressable
      onPress={() => onPress(estacao)}
      accessibilityRole="button"
      accessibilityLabel={`Estação ${estacao.nome}, ${codigos}${comAlerta ? ', com alerta de status' : ''}`}
      accessibilityHint="Abre os detalhes da estação"
      className="mb-2.5 min-h-[64px] flex-row items-center gap-3 rounded-2xl border border-border bg-surface px-4 py-3 active:opacity-80"
    >
      <View className="flex-1 gap-1.5">
        <View className="flex-row items-center gap-1.5">
          <Text className="flex-shrink text-base font-semibold text-ink" numberOfLines={1}>
            {estacao.nome}
          </Text>
          {comAlerta && <TriangleAlert size={15} color={colors.warning.DEFAULT} />}
        </View>
        {linhas.length > 0 && (
          <View className="flex-row flex-wrap gap-1">
            {linhas.map((l) => (
              <LinhaBadge key={l.id} codigo={l.codigo} cor={l.cor} tamanho="sm" />
            ))}
          </View>
        )}
      </View>
      <ChevronRight size={18} color={colors.ink.subtle} />
    </Pressable>
  );
});
