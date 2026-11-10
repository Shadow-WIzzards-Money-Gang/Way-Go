import { type LucideIcon, WifiOff } from 'lucide-react-native';
import { Text, View } from 'react-native';

import { ApiError } from '@/api/errors';
import { colors } from '@/theme/colors';

import { Button } from './Button';

interface EmptyStateProps {
  icone: LucideIcon;
  titulo: string;
  mensagem?: string;
  acao?: { rotulo: string; onPress: () => void };
}

export function EmptyState({ icone: Icone, titulo, mensagem, acao }: EmptyStateProps) {
  return (
    <View className="items-center px-8 py-12">
      <View className="mb-4 h-14 w-14 items-center justify-center rounded-full bg-primary-soft">
        <Icone size={26} color={colors.primary.DEFAULT} />
      </View>
      <Text className="text-center text-lg font-semibold text-ink">{titulo}</Text>
      {mensagem && <Text className="mt-1 text-center text-sm text-ink-muted">{mensagem}</Text>}
      {acao && (
        <Button rotulo={acao.rotulo} onPress={acao.onPress} variante="outline" className="mt-5" />
      )}
    </View>
  );
}

interface ErrorStateProps {
  erro: unknown;
  aoTentarNovamente: () => void;
}

export function ErrorState({ erro, aoTentarNovamente }: ErrorStateProps) {
  const mensagem =
    erro instanceof ApiError ? erro.message : 'Não foi possível carregar as informações.';
  return (
    <EmptyState
      icone={WifiOff}
      titulo="Algo deu errado"
      mensagem={mensagem}
      acao={{ rotulo: 'Tentar novamente', onPress: aoTentarNovamente }}
    />
  );
}
