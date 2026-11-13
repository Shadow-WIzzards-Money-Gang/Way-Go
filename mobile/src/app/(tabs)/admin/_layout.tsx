import { Redirect } from 'expo-router';
import Stack from 'expo-router/stack';
import { ActivityIndicator, View } from 'react-native';

import { useUsuarioAtual } from '@/hooks/queries';
import { useLogado } from '@/store/sessao';
import { colors } from '@/theme/colors';

export const unstable_settings = { initialRouteName: 'index' };

/**
 * Área do administrador. A aba já fica oculta para os demais, mas um link direto também é
 * barrado aqui; de todo modo, o backend recusa (403) qualquer escrita sem a role.
 */
export default function AdminLayout() {
  const logado = useLogado();
  const { data: usuario, isPending, error } = useUsuarioAtual();

  if (!logado || error || (usuario && !usuario.roles.includes('ADMINISTRADOR'))) {
    return <Redirect href="/" />;
  }
  if (isPending) {
    return (
      <View className="flex-1 items-center justify-center bg-background">
        <ActivityIndicator color={colors.primary.DEFAULT} />
      </View>
    );
  }

  return (
    <Stack
      screenOptions={{
        headerShown: true,
        headerTintColor: colors.primary.DEFAULT,
        headerTitleStyle: { color: colors.ink.DEFAULT },
        headerStyle: { backgroundColor: colors.surface },
        headerBackButtonDisplayMode: 'minimal',
      }}
    >
      <Stack.Screen name="index" options={{ headerShown: false }} />
      <Stack.Screen name="usuarios" options={{ title: 'Usuários' }} />
      <Stack.Screen name="linhas/index" options={{ title: 'Linhas' }} />
      <Stack.Screen name="linhas/nova" options={{ title: 'Nova linha' }} />
      <Stack.Screen name="linhas/[id]" options={{ title: 'Editar linha' }} />
      <Stack.Screen name="estacoes/index" options={{ title: 'Estações' }} />
      <Stack.Screen name="estacoes/nova" options={{ title: 'Nova estação' }} />
      <Stack.Screen name="estacoes/[id]" options={{ title: 'Editar estação' }} />
    </Stack>
  );
}
