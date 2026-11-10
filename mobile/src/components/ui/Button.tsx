import * as Haptics from 'expo-haptics';
import type { LucideIcon } from 'lucide-react-native';
import { ActivityIndicator, Pressable, Text } from 'react-native';

import { colors } from '@/theme/colors';

type Variante = 'primary' | 'outline' | 'ghost' | 'danger';

const ESTILOS: Record<Variante, { container: string; texto: string; icone: string }> = {
  primary: {
    container: 'bg-primary active:bg-primary-pressed',
    texto: 'text-white',
    icone: '#FFFFFF',
  },
  outline: {
    container: 'border border-primary bg-surface active:bg-primary-soft',
    texto: 'text-primary',
    icone: colors.primary.DEFAULT,
  },
  ghost: { container: 'active:bg-neutral-soft', texto: 'text-ink', icone: colors.ink.DEFAULT },
  danger: {
    container: 'active:bg-danger-soft',
    texto: 'text-danger',
    icone: colors.danger.DEFAULT,
  },
};

interface ButtonProps {
  rotulo: string;
  onPress: () => void;
  variante?: Variante;
  icone?: LucideIcon;
  carregando?: boolean;
  desabilitado?: boolean;
  compacto?: boolean;
  className?: string;
  accessibilityHint?: string;
}

export function Button({
  rotulo,
  onPress,
  variante = 'primary',
  icone: Icone,
  carregando = false,
  desabilitado = false,
  compacto = false,
  className = '',
  accessibilityHint,
}: ButtonProps) {
  const estilo = ESTILOS[variante];
  const inativo = desabilitado || carregando;

  const aoPressionar = () => {
    if (variante === 'primary') {
      void Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light).catch(() => undefined);
    }
    onPress();
  };

  return (
    <Pressable
      onPress={aoPressionar}
      disabled={inativo}
      accessibilityRole="button"
      accessibilityLabel={rotulo}
      accessibilityHint={accessibilityHint}
      accessibilityState={{ disabled: inativo, busy: carregando }}
      hitSlop={compacto ? 8 : undefined}
      className={`flex-row items-center justify-center gap-1.5 rounded-xl ${
        compacto ? 'min-h-[36px] px-2' : 'min-h-[48px] px-4'
      } ${estilo.container} ${inativo ? 'opacity-50' : ''} ${className}`}
    >
      {carregando ? (
        <ActivityIndicator color={estilo.icone} size="small" />
      ) : (
        Icone && <Icone size={compacto ? 16 : 18} color={estilo.icone} />
      )}
      <Text className={`font-semibold ${compacto ? 'text-sm' : 'text-base'} ${estilo.texto}`}>
        {rotulo}
      </Text>
    </Pressable>
  );
}
