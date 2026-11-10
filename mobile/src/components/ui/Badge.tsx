import { Text, View } from 'react-native';

import type { Tom } from '@/utils/status';

const ESTILOS: Record<Tom, { fundo: string; texto: string }> = {
  success: { fundo: 'bg-success-soft', texto: 'text-success' },
  warning: { fundo: 'bg-warning-soft', texto: 'text-warning' },
  danger: { fundo: 'bg-danger-soft', texto: 'text-danger' },
  neutral: { fundo: 'bg-neutral-soft', texto: 'text-neutral' },
};

interface BadgeProps {
  rotulo: string;
  tom: Tom;
}

export function Badge({ rotulo, tom }: BadgeProps) {
  const estilo = ESTILOS[tom];
  return (
    <View className={`self-start rounded-full px-2.5 py-1 ${estilo.fundo}`}>
      <Text className={`text-xs font-semibold ${estilo.texto}`} numberOfLines={1}>
        {rotulo}
      </Text>
    </View>
  );
}
