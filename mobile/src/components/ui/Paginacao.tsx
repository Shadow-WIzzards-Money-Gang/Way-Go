import { ChevronLeft, ChevronRight, type LucideIcon } from 'lucide-react-native';
import { ActivityIndicator, Pressable, Text, View } from 'react-native';

import { colors } from '@/theme/colors';

interface PaginacaoProps {
  /** Página atual, começando em 0 (como o backend). */
  pagina: number;
  totalPaginas: number;
  aoMudar: (pagina: number) => void;
  /** Mostra um indicador enquanto a página pedida carrega. */
  carregando?: boolean;
}

function BotaoPagina({
  icone: Icone,
  rotulo,
  desabilitado,
  onPress,
}: {
  icone: LucideIcon;
  rotulo: string;
  desabilitado: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      disabled={desabilitado}
      accessibilityRole="button"
      accessibilityLabel={rotulo}
      accessibilityState={{ disabled: desabilitado }}
      hitSlop={6}
      className={`h-11 w-11 items-center justify-center rounded-xl border border-border bg-surface active:bg-neutral-soft ${
        desabilitado ? 'opacity-40' : ''
      }`}
    >
      <Icone size={20} color={colors.ink.DEFAULT} />
    </Pressable>
  );
}

/** Barra "‹ Página X de Y ›" para listas paginadas. Some quando só há uma página. */
export function Paginacao({ pagina, totalPaginas, aoMudar, carregando = false }: PaginacaoProps) {
  if (totalPaginas <= 1) return null;

  const naPrimeira = pagina <= 0;
  const naUltima = pagina >= totalPaginas - 1;

  return (
    <View className="flex-row items-center justify-between gap-3 border-t border-border bg-surface px-4 py-2.5">
      <BotaoPagina
        icone={ChevronLeft}
        rotulo="Página anterior"
        desabilitado={naPrimeira}
        onPress={() => aoMudar(pagina - 1)}
      />
      <View className="flex-row items-center gap-2">
        {carregando && <ActivityIndicator size="small" color={colors.primary.DEFAULT} />}
        <Text className="text-sm font-medium text-ink" accessibilityLiveRegion="polite">
          Página {pagina + 1} de {totalPaginas}
        </Text>
      </View>
      <BotaoPagina
        icone={ChevronRight}
        rotulo="Próxima página"
        desabilitado={naUltima}
        onPress={() => aoMudar(pagina + 1)}
      />
    </View>
  );
}
