import { CircleAlert } from 'lucide-react-native';
import { Text, View } from 'react-native';

import { colors } from '@/theme/colors';

/** Faixa vermelha para erros de formulário (ex.: "E-mail ou senha inválidos"). */
export function AlertaErro({ mensagem }: { mensagem: string }) {
  return (
    <View
      className="flex-row items-start gap-2.5 rounded-xl bg-danger-soft p-3"
      accessibilityRole="alert"
      accessibilityLiveRegion="assertive"
    >
      <CircleAlert size={18} color={colors.danger.DEFAULT} style={{ marginTop: 1 }} />
      <Text className="flex-1 text-sm leading-5 text-danger">{mensagem}</Text>
    </View>
  );
}
