import Stack from 'expo-router/stack';

// Garante que "Linhas" fique por baixo ao abrir uma linha a partir de outra aba (ex.: Estações).
export const unstable_settings = { initialRouteName: 'index' };

export default function LinhasLayout() {
  return <Stack screenOptions={{ headerShown: false }} />;
}
