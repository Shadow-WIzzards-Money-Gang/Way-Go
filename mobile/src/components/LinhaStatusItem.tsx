import { memo } from 'react';
import { Pressable, Text, View } from 'react-native';

import type { LinhaRede } from '@/types/api';
import { infoStatus } from '@/utils/status';
import { formatarNomeLinha } from '@/utils/text';

import { StatusBadge } from './StatusBadge';

interface LinhaStatusItemProps {
  linha: LinhaRede;
  extremos: string | null;
  onPress: (linha: LinhaRede) => void;
}

/** Card da lista "Todas as linhas": barra na cor da linha, nome, extremos e status. */
export const LinhaStatusItem = memo(function LinhaStatusItem({
  linha,
  extremos,
  onPress,
}: LinhaStatusItemProps) {
  const nome = formatarNomeLinha(linha.nome);
  return (
    <Pressable
      onPress={() => onPress(linha)}
      accessibilityRole="button"
      accessibilityLabel={`${nome}, ${infoStatus(linha.status).rotulo}`}
      accessibilityHint="Abre as estações da linha"
      className="mb-3 min-h-[64px] flex-row items-center gap-3 rounded-2xl border border-border bg-surface p-4 active:opacity-80"
    >
      <View className="w-1.5 self-stretch rounded-full" style={{ backgroundColor: linha.cor }} />
      <View className="flex-1">
        <Text className="text-base font-semibold text-ink" numberOfLines={1}>
          {nome}
        </Text>
        {extremos && (
          <Text className="mt-0.5 text-xs text-ink-muted" numberOfLines={1}>
            {extremos}
          </Text>
        )}
      </View>
      <StatusBadge status={linha.status} />
    </Pressable>
  );
});
