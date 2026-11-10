import { memo } from 'react';
import { Text, View } from 'react-native';

import { LinhasChip } from './LinhaBadge';

interface EstacaoTimelineItemProps {
  nome: string;
  cor: string;
  ativa: boolean;
  primeira: boolean;
  ultima: boolean;
  destaque?: boolean;
  outrasLinhas: string[];
}

/** Uma estação na linha do tempo vertical da tela da linha. */
export const EstacaoTimelineItem = memo(function EstacaoTimelineItem({
  nome,
  cor,
  ativa,
  primeira,
  ultima,
  destaque = false,
  outrasLinhas,
}: EstacaoTimelineItemProps) {
  const ehBaldeacao = outrasLinhas.length > 0;
  return (
    <View
      className={`min-h-[44px] flex-row items-center ${ativa ? '' : 'opacity-40'}`}
      accessibilityLabel={`${nome}${ehBaldeacao ? `, baldeação com ${outrasLinhas.map((c) => `linha ${c}`).join(', ')}` : ''}${ativa ? '' : ', estação inativa'}`}
    >
      <View className="w-6 items-center self-stretch">
        <View className="w-1 flex-1" style={{ backgroundColor: primeira ? 'transparent' : cor }} />
        <View
          className="h-3.5 w-3.5 rounded-full border-[3px] bg-surface"
          style={{ borderColor: cor }}
        />
        <View className="w-1 flex-1" style={{ backgroundColor: ultima ? 'transparent' : cor }} />
      </View>
      <Text
        className={`ml-3 flex-1 text-base text-ink ${ehBaldeacao || destaque ? 'font-semibold' : ''}`}
        numberOfLines={1}
      >
        {nome}
      </Text>
      <LinhasChip codigos={outrasLinhas} />
    </View>
  );
});
