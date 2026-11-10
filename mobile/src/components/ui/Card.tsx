import type { ReactNode } from 'react';
import { Pressable, View } from 'react-native';

interface CardProps {
  children: ReactNode;
  className?: string;
  onPress?: () => void;
  accessibilityLabel?: string;
}

const BASE = 'rounded-2xl border border-border bg-surface p-4';

export function Card({ children, className = '', onPress, accessibilityLabel }: CardProps) {
  if (onPress) {
    return (
      <Pressable
        onPress={onPress}
        accessibilityRole="button"
        accessibilityLabel={accessibilityLabel}
        className={`${BASE} active:opacity-80 ${className}`}
      >
        {children}
      </Pressable>
    );
  }
  return <View className={`${BASE} ${className}`}>{children}</View>;
}
