import '../global.css';

import { QueryClientProvider } from '@tanstack/react-query';
import { DefaultTheme, ThemeProvider } from 'expo-router';
import Stack from 'expo-router/stack';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';

import { useAlertasRotas } from '@/hooks/useAlertasRotas';
import { configurarFocoDoApp, queryClient } from '@/lib/queryClient';
import { colors } from '@/theme/colors';

const tema = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    primary: colors.primary.DEFAULT,
    background: colors.background,
    card: colors.surface,
    text: colors.ink.DEFAULT,
    border: colors.border,
  },
};

function AlertasDeRotas() {
  useAlertasRotas();
  return null;
}

export default function RootLayout() {
  useEffect(() => configurarFocoDoApp(), []);

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider value={tema}>
        <StatusBar style="dark" />
        <AlertasDeRotas />
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="(tabs)" />
          <Stack.Screen
            name="selecionar-estacao"
            options={{ presentation: 'modal', headerShown: true, title: 'Escolher estação' }}
          />
          <Stack.Screen
            name="rota-salva"
            options={{ presentation: 'modal', headerShown: true, title: 'Rota salva' }}
          />
        </Stack>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
