import { Eye, EyeOff } from 'lucide-react-native';
import { type Ref, useState } from 'react';
import {
  Pressable,
  Text,
  TextInput,
  type TextInputInstance,
  type TextInputProps,
  View,
} from 'react-native';

import { colors } from '@/theme/colors';

interface TextFieldProps extends Omit<TextInputProps, 'className' | 'style' | 'secureTextEntry'> {
  rotulo: string;
  dica?: string;
  erro?: string | null;
  /** Campo de senha, com botão para mostrar/ocultar. */
  senha?: boolean;
  ref?: Ref<TextInputInstance>;
  className?: string;
}

/** Campo com rótulo, dica e mensagem de erro, no padrão visual dos formulários do app. */
export function TextField({
  rotulo,
  dica,
  erro,
  senha = false,
  ref,
  className = '',
  onFocus,
  onBlur,
  ...props
}: TextFieldProps) {
  const [focado, setFocado] = useState(false);
  const [visivel, setVisivel] = useState(false);

  const borda = erro ? 'border-danger' : focado ? 'border-primary' : 'border-border';

  return (
    <View className={className}>
      <Text className="mb-1.5 text-sm font-medium text-ink">{rotulo}</Text>
      <View className={`min-h-[52px] flex-row items-center rounded-xl border bg-surface ${borda}`}>
        <TextInput
          ref={ref}
          placeholderTextColor={colors.ink.subtle}
          secureTextEntry={senha && !visivel}
          accessibilityLabel={rotulo}
          accessibilityHint={erro ?? dica}
          onFocus={(e) => {
            setFocado(true);
            onFocus?.(e);
          }}
          onBlur={(e) => {
            setFocado(false);
            onBlur?.(e);
          }}
          className="flex-1 px-3 py-3 text-base text-ink"
          {...props}
        />
        {senha && (
          <Pressable
            onPress={() => setVisivel((v) => !v)}
            accessibilityRole="button"
            accessibilityLabel={visivel ? 'Ocultar senha' : 'Mostrar senha'}
            className="h-11 w-11 items-center justify-center"
          >
            {visivel ? (
              <EyeOff size={20} color={colors.ink.muted} />
            ) : (
              <Eye size={20} color={colors.ink.muted} />
            )}
          </Pressable>
        )}
      </View>
      {erro ? (
        <Text className="mt-1.5 text-xs text-danger" accessibilityLiveRegion="polite">
          {erro}
        </Text>
      ) : (
        dica && <Text className="mt-1.5 text-xs text-ink-subtle">{dica}</Text>
      )}
    </View>
  );
}
