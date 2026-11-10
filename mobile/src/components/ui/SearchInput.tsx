import { Search, X } from 'lucide-react-native';
import { Pressable, TextInput, View } from 'react-native';

import { colors } from '@/theme/colors';

interface SearchInputProps {
  valor: string;
  aoMudar: (texto: string) => void;
  placeholder: string;
  autoFocus?: boolean;
}

export function SearchInput({ valor, aoMudar, placeholder, autoFocus }: SearchInputProps) {
  return (
    <View className="min-h-[48px] flex-row items-center gap-2 rounded-xl border border-border bg-surface px-3">
      <Search size={18} color={colors.ink.subtle} />
      <TextInput
        value={valor}
        onChangeText={aoMudar}
        placeholder={placeholder}
        placeholderTextColor={colors.ink.subtle}
        autoFocus={autoFocus}
        autoCorrect={false}
        autoCapitalize="none"
        returnKeyType="search"
        accessibilityLabel={placeholder}
        className="flex-1 py-3 text-base text-ink"
      />
      {valor.length > 0 && (
        <Pressable
          onPress={() => aoMudar('')}
          hitSlop={12}
          accessibilityRole="button"
          accessibilityLabel="Limpar busca"
        >
          <X size={18} color={colors.ink.subtle} />
        </Pressable>
      )}
    </View>
  );
}
