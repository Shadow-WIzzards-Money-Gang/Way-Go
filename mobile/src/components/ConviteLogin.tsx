import { LogIn, type LucideIcon } from 'lucide-react-native';
import { Text, View } from 'react-native';

import { abrirCadastro, abrirLogin, type ParametrosLogin } from '@/lib/conta';
import { colors } from '@/theme/colors';

import { Button } from './ui/Button';

interface ConviteLoginProps {
  titulo: string;
  mensagem: string;
  icone?: LucideIcon;
  /** Repassado ao login/cadastro (motivo e para onde voltar). */
  parametros?: ParametrosLogin;
}

/** Mensagem amigável para quem tenta usar uma função da conta sem estar logado. */
export function ConviteLogin({ titulo, mensagem, icone: Icone = LogIn, parametros }: ConviteLoginProps) {
  return (
    <View className="items-center px-6 py-12">
      <View className="mb-4 h-16 w-16 items-center justify-center rounded-full bg-primary-soft">
        <Icone size={28} color={colors.primary.DEFAULT} />
      </View>
      <Text className="text-center text-xl font-semibold text-ink" accessibilityRole="header">
        {titulo}
      </Text>
      <Text className="mt-1.5 text-center text-sm leading-5 text-ink-muted">{mensagem}</Text>
      <View className="mt-6 w-full max-w-xs gap-3">
        <Button rotulo="Entrar" onPress={() => abrirLogin(parametros)} />
        <Button rotulo="Criar conta" variante="outline" onPress={() => abrirCadastro(parametros)} />
      </View>
    </View>
  );
}
