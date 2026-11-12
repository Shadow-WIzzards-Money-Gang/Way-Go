import '../global.css';

import { QueryClientProvider } from '@tanstack/react-query';
import { DefaultTheme, ThemeProvider } from 'expo-router';
import Stack from 'expo-router/stack';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';

import { definirAoNaoAutorizado } from '@/api/client';
import { useAlertasRotas } from '@/hooks/useAlertasRotas';
import { abrirLogin, descartarRotasAntigasDoAparelho, descartarSessaoExpirada } from '@/lib/conta';
import { configurarFocoDoApp, queryClient } from '@/lib/queryClient';
import { useSessao } from '@/store/sessao';
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

/** Qualquer 401 de um endpoint protegido abre o login (com aviso se a sessão expirou). */
function tratarNaoAutorizado() {
  const tinhaSessao = useSessao.getState().sessao !== null;
  void descartarSessaoExpirada();
  abrirLogin({ motivo: tinhaSessao ? 'sessao-expirada' : 'restrito' });
}

export default function RootLayout() {
  useEffect(() => configurarFocoDoApp(), []);
  useEffect(() => {
    void useSessao.getState().carregar();
    void descartarRotasAntigasDoAparelho();
    return definirAoNaoAutorizado(tratarNaoAutorizado);
  }, []);

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
          <Stack.Screen name="login" options={{ presentation: 'modal' }} />
          <Stack.Screen name="cadastro" options={{ presentation: 'modal' }} />
          <Stack.Screen
            name="conta"
            options={{ presentation: 'modal', headerShown: true, title: 'Minha conta' }}
          />
        </Stack>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
