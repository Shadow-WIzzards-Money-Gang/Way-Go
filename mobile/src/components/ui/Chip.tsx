import { Pressable, Text } from 'react-native';

interface ChipProps {
  rotulo: string;
  selecionado: boolean;
  onPress: () => void;
}

export function Chip({ rotulo, selecionado, onPress }: ChipProps) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityState={{ selected: selecionado }}
      className={`min-h-[36px] items-center justify-center rounded-full border px-3.5 ${
        selecionado
          ? 'border-primary bg-primary-soft'
          : 'border-border bg-surface active:bg-neutral-soft'
      }`}
    >
      <Text className={`text-sm font-medium ${selecionado ? 'text-primary' : 'text-ink-muted'}`}>
        {rotulo}
      </Text>
    </Pressable>
  );
}
