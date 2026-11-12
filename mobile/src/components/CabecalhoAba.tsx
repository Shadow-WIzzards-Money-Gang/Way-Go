import type { ReactNode } from 'react';
import { Text, View } from 'react-native';

import { AvatarConta } from './AvatarConta';

interface CabecalhoAbaProps {
  titulo: string;
  subtitulo?: string | null;
  /** Botões extras à esquerda do avatar (ex.: "Nova rota"). */
  acoes?: ReactNode;
}

/** Título grande da aba com o avatar da conta no canto direito. */
export function CabecalhoAba({ titulo, subtitulo, acoes }: CabecalhoAbaProps) {
  return (
    <View className="flex-row items-center gap-2">
      <View className="flex-1">
        <Text className="text-3xl font-bold text-ink" accessibilityRole="header" numberOfLines={1}>
          {titulo}
        </Text>
        {subtitulo && <Text className="mt-0.5 text-sm text-ink-muted">{subtitulo}</Text>}
      </View>
      {acoes}
      <AvatarConta />
    </View>
  );
}
