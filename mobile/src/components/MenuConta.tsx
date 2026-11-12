import * as Haptics from 'expo-haptics';
import { router } from 'expo-router';
import { ChevronRight, LogOut, type LucideIcon, UserRound } from 'lucide-react-native';
import { useRef } from 'react';
import { Alert, Modal, Platform, Pressable, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { abrirCadastro, abrirLogin, encerrarSessao } from '@/lib/conta';
import { useEmailDaSessao } from '@/store/sessao';
import { colors } from '@/theme/colors';

import { Avatar } from './ui/Avatar';
import { Button } from './ui/Button';

interface MenuContaProps {
  visivel: boolean;
  aoFechar: () => void;
}

function ItemMenu({
  icone: Icone,
  rotulo,
  perigo = false,
  onPress,
}: {
  icone: LucideIcon;
  rotulo: string;
  perigo?: boolean;
  onPress: () => void;
}) {
  const cor = perigo ? colors.danger.DEFAULT : colors.ink.DEFAULT;
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={rotulo}
      className={`min-h-[48px] flex-row items-center gap-3 rounded-xl px-3 ${
        perigo ? 'active:bg-danger-soft' : 'active:bg-neutral-soft'
      }`}
    >
      <Icone size={20} color={cor} />
      <Text className={`flex-1 text-base font-medium ${perigo ? 'text-danger' : 'text-ink'}`}>
        {rotulo}
      </Text>
      {!perigo && <ChevronRight size={18} color={colors.ink.subtle} />}
    </Pressable>
  );
}

/** Pop-up ancorado no canto superior direito, abaixo do avatar. */
export function MenuConta({ visivel, aoFechar }: MenuContaProps) {
  const insets = useSafeAreaInsets();
  const email = useEmailDaSessao();
  // No iOS, abrir outra tela enquanto o Modal fecha falha; a ação espera o onDismiss.
  const pendente = useRef<(() => void) | null>(null);

  const fecharE = (acao: () => void) => {
    if (Platform.OS === 'ios') {
      pendente.current = acao;
      aoFechar();
    } else {
      aoFechar();
      acao();
    }
  };

  const executarPendente = () => {
    const acao = pendente.current;
    pendente.current = null;
    acao?.();
  };

  const confirmarSaida = () =>
    Alert.alert('Encerrar sessão?', 'Suas rotas continuam salvas na conta.', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Encerrar sessão',
        style: 'destructive',
        onPress: () => {
          void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
            () => undefined,
          );
          void encerrarSessao();
        },
      },
    ]);

  return (
    <Modal
      visible={visivel}
      transparent
      animationType="fade"
      onRequestClose={aoFechar}
      onDismiss={executarPendente}
      statusBarTranslucent
    >
      <Pressable
        onPress={aoFechar}
        accessibilityRole="button"
        accessibilityLabel="Fechar menu da conta"
        className="flex-1 bg-ink/30"
      >
        <View
          className="absolute right-4 w-72 rounded-2xl border border-border bg-surface p-2 shadow-lg"
          style={{ top: insets.top + 60, elevation: 8 }}
          // Toques dentro do card não fecham o menu.
          onStartShouldSetResponder={() => true}
          accessibilityViewIsModal
        >
          {email ? (
            <>
              <View className="flex-row items-center gap-3 px-3 py-3">
                <Avatar email={email} />
                <View className="flex-1">
                  <Text className="text-xs text-ink-muted">Conectado como</Text>
                  <Text className="text-base font-semibold text-ink" numberOfLines={1}>
                    {email}
                  </Text>
                </View>
              </View>
              <View className="mx-3 mb-1 h-px bg-border" />
              <ItemMenu
                icone={UserRound}
                rotulo="Minha conta"
                onPress={() => fecharE(() => router.push('/conta'))}
              />
              <ItemMenu
                icone={LogOut}
                rotulo="Encerrar sessão"
                perigo
                onPress={() => fecharE(confirmarSaida)}
              />
            </>
          ) : (
            <View className="gap-3 p-3">
              <View>
                <Text className="text-base font-semibold text-ink">Você não está conectado</Text>
                <Text className="mt-0.5 text-sm text-ink-muted">
                  Entre para salvar rotas e acessá-las em qualquer aparelho.
                </Text>
              </View>
              <Button rotulo="Entrar" onPress={() => fecharE(() => abrirLogin())} />
              <Button
                rotulo="Criar conta"
                variante="outline"
                onPress={() => fecharE(() => abrirCadastro())}
              />
            </View>
          )}
        </View>
      </Pressable>
    </Modal>
  );
}
