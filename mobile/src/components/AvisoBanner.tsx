import { TriangleAlert } from 'lucide-react-native';
import { Text, View } from 'react-native';

import { colors } from '@/theme/colors';

interface AvisoBannerProps {
  titulo?: string;
  mensagem: string;
}

/** Faixa laranja de alerta (atrasos, velocidade reduzida etc.). */
export function AvisoBanner({ titulo, mensagem }: AvisoBannerProps) {
  return (
    <View
      className="flex-row gap-2.5 rounded-2xl border border-warning-border bg-warning-bg p-3.5"
      accessibilityRole="alert"
    >
      <TriangleAlert size={18} color={colors.warning.DEFAULT} style={{ marginTop: 1 }} />
      <View className="flex-1">
        {titulo && <Text className="font-semibold text-warning">{titulo}</Text>}
        <Text className="text-sm leading-5 text-warning">{mensagem}</Text>
      </View>
    </View>
  );
}
