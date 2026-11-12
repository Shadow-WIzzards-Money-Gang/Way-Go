import Stack from 'expo-router/stack';

// Garante que a lista fique por baixo ao abrir uma estação direto (ex.: por link).
export const unstable_settings = { initialRouteName: 'index' };

export default function EstacoesLayout() {
  return <Stack screenOptions={{ headerShown: false }} />;
}
