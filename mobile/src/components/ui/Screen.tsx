import type { ReactNode } from 'react';
import { View } from 'react-native';
import { type Edge, SafeAreaView } from 'react-native-safe-area-context';

interface ScreenProps {
  children: ReactNode;
  edges?: Edge[];
  className?: string;
}

/** Contêiner base de toda tela: respeita safe area e aplica o fundo padrão. */
export function Screen({ children, edges = ['top'], className = '' }: ScreenProps) {
  return (
    <SafeAreaView edges={edges} className="flex-1 bg-background">
      <View className={`flex-1 ${className}`}>{children}</View>
    </SafeAreaView>
  );
}
