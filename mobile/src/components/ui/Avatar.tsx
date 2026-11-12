import { UserRound } from 'lucide-react-native';
import { Text, View } from 'react-native';

import { colors } from '@/theme/colors';
import { inicialDoEmail } from '@/utils/text';

interface AvatarProps {
  email: string | null;
  tamanho?: 'md' | 'lg';
}

/** Círculo com a inicial do e-mail (logado) ou um ícone de pessoa (visitante). */
export function Avatar({ email, tamanho = 'md' }: AvatarProps) {
  const dimensao = tamanho === 'lg' ? 'h-20 w-20' : 'h-11 w-11';
  if (!email) {
    return (
      <View
        className={`${dimensao} items-center justify-center rounded-full border border-border bg-surface`}
      >
        <UserRound size={tamanho === 'lg' ? 36 : 22} color={colors.ink.muted} />
      </View>
    );
  }
  return (
    <View className={`${dimensao} items-center justify-center rounded-full bg-primary`}>
      <Text className={`font-bold text-white ${tamanho === 'lg' ? 'text-3xl' : 'text-lg'}`}>
        {inicialDoEmail(email)}
      </Text>
    </View>
  );
}
