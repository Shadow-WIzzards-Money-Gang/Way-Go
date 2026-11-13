import type { ReactNode } from 'react';
import { View } from 'react-native';

import { colors } from '@/theme/colors';

/** Painel branco ancorado na base da tela (sobre o mapa), com cantos superiores arredondados. */
export function PainelInferior({
  children,
  className = '',
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <View
      className={`rounded-t-3xl bg-surface px-4 pb-4 ${className}`}
      style={{
        shadowColor: colors.ink.DEFAULT,
        shadowOpacity: 0.1,
        shadowRadius: 20,
        shadowOffset: { width: 0, height: -4 },
        elevation: 12,
      }}
    >
      {children}
    </View>
  );
}
