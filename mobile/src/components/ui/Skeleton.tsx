import { useEffect } from 'react';
import { View } from 'react-native';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withRepeat,
  withTiming,
} from 'react-native-reanimated';

import { colors } from '@/theme/colors';

interface SkeletonProps {
  className?: string;
}

/** Bloco pulsante usado no lugar de spinners enquanto os dados carregam. */
export function Skeleton({ className = '' }: SkeletonProps) {
  const opacidade = useSharedValue(0.5);

  useEffect(() => {
    opacidade.value = withRepeat(withTiming(1, { duration: 700 }), -1, true);
  }, [opacidade]);

  const estilo = useAnimatedStyle(() => ({ opacity: opacidade.value }));

  // className fica no View (NativeWind); a animação fica só no Animated.View interno.
  return (
    <View
      className={`overflow-hidden rounded-xl ${className}`}
      accessibilityElementsHidden
      importantForAccessibility="no-hide-descendants"
    >
      <Animated.View style={[{ flex: 1, backgroundColor: colors.border }, estilo]} />
    </View>
  );
}

export function SkeletonLista({
  itens = 5,
  className = 'h-16',
}: {
  itens?: number;
  className?: string;
}) {
  return (
    <>
      {Array.from({ length: itens }, (_, i) => (
        <Skeleton key={i} className={`mb-3 ${className}`} />
      ))}
    </>
  );
}
