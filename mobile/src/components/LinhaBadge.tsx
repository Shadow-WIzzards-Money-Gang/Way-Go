import { Text, View } from 'react-native';

import { corDeTextoSobre } from '@/utils/color';

interface LinhaBadgeProps {
  codigo: string;
  cor: string;
  tamanho?: 'sm' | 'md';
}

/** Quadrado com o número da linha na cor oficial (ex.: "11" laranja). */
export function LinhaBadge({ codigo, cor, tamanho = 'md' }: LinhaBadgeProps) {
  const dimensao = tamanho === 'sm' ? 'h-6 min-w-[24px] rounded-md' : 'h-8 min-w-[32px] rounded-lg';
  return (
    <View
      className={`items-center justify-center px-1.5 ${dimensao}`}
      style={{ backgroundColor: cor }}
      accessibilityLabel={`Linha ${codigo}`}
    >
      <Text
        className={`font-bold ${tamanho === 'sm' ? 'text-xs' : 'text-sm'}`}
        style={{ color: corDeTextoSobre(cor) }}
      >
        {codigo}
      </Text>
    </View>
  );
}

/** Chip cinza "L3 · L7" com as outras linhas que passam na estação. */
export function LinhasChip({ codigos }: { codigos: string[] }) {
  if (codigos.length === 0) return null;
  return (
    <View className="rounded-md bg-neutral-soft px-2 py-0.5">
      <Text className="text-xs font-medium text-ink-muted">
        {codigos.map((c) => `L${c}`).join(' · ')}
      </Text>
    </View>
  );
}
