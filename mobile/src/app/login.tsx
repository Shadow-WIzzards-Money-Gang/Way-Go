import * as Haptics from 'expo-haptics';
import { Image } from 'expo-image';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  type TextInputInstance,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { AvisoBanner } from '@/components/AvisoBanner';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/TextField';
import { useLogin } from '@/hooks/queries';
import { abrirCadastro, concluirEntrada, marcarLoginAberto, type MotivoLogin } from '@/lib/conta';
import { ehEmailValido } from '@/utils/text';

const LOGO = require('@/assets/images/logo-waygo.png');

const SUBTITULOS: Record<MotivoLogin | 'padrao', string> = {
  padrao: 'Acesse suas rotas salvas e receba alertas das linhas que você usa.',
  rotas: 'Entre para ver as rotas salvas na sua conta.',
  'salvar-rota': 'Entre para salvar esta rota e acessá-la em qualquer aparelho.',
  restrito: 'Entre na sua conta para continuar.',
  'sessao-expirada': 'Entre novamente para continuar de onde parou.',
};

export default function LoginScreen() {
  const { motivo, proximo } = useLocalSearchParams<{ motivo?: MotivoLogin; proximo?: string }>();
  const login = useLogin();
  const senhaRef = useRef<TextInputInstance>(null);

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [tentou, setTentou] = useState(false);

  useEffect(() => {
    marcarLoginAberto(true);
    return () => marcarLoginAberto(false);
  }, []);

  const erroEmail = !email.trim()
    ? 'Informe seu e-mail.'
    : !ehEmailValido(email)
      ? 'Confira o e-mail digitado.'
      : null;
  const erroSenha = senha ? null : 'Informe sua senha.';

  const erroApi =
    login.error instanceof ApiError
      ? login.error.tipo === 'nao-autorizado'
        ? 'E-mail ou senha incorretos.'
        : login.error.message
      : login.error
        ? 'Não foi possível entrar. Tente novamente.'
        : null;

  const entrar = () => {
    setTentou(true);
    if (erroEmail || erroSenha || login.isPending) return;
    login.mutate(
      { email, senha },
      {
        onSuccess: () => {
          void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
            () => undefined,
          );
          concluirEntrada(proximo, 1);
        },
      },
    );
  };

  const fechar = () => (router.canGoBack() ? router.back() : router.replace('/'));

  return (
    <SafeAreaView className="flex-1 bg-background">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView
          contentContainerClassName="flex-grow gap-5 px-5 pb-8 pt-10"
          keyboardShouldPersistTaps="handled"
        >
          <View className="flex-row items-center gap-3" accessible accessibilityLabel="WayGo">
            <Image
              source={LOGO}
              style={{ width: 56, height: 56, borderRadius: 14 }}
              contentFit="cover"
              accessibilityIgnoresInvertColors
            />
            <Text className="text-2xl font-bold text-ink">WayGo</Text>
          </View>

          <View className="mt-4">
            <Text className="text-4xl font-bold text-ink" accessibilityRole="header">
              Entrar
            </Text>
            <Text className="mt-2 text-base leading-6 text-ink-muted">
              {SUBTITULOS[motivo ?? 'padrao'] ?? SUBTITULOS.padrao}
            </Text>
          </View>

          {motivo === 'sessao-expirada' && (
            <AvisoBanner mensagem="Sua sessão expirou por segurança." />
          )}

          <TextField
            rotulo="E-mail"
            value={email}
            onChangeText={setEmail}
            placeholder="voce@email.com"
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            autoComplete="email"
            textContentType="emailAddress"
            returnKeyType="next"
            onSubmitEditing={() => senhaRef.current?.focus()}
            submitBehavior="submit"
            erro={tentou ? erroEmail : null}
          />
          <TextField
            ref={senhaRef}
            rotulo="Senha"
            senha
            value={senha}
            onChangeText={setSenha}
            placeholder="Sua senha"
            autoCapitalize="none"
            autoComplete="current-password"
            textContentType="password"
            returnKeyType="go"
            onSubmitEditing={entrar}
            erro={tentou ? erroSenha : null}
          />

          {erroApi && <AlertaErro mensagem={erroApi} />}

          <Button rotulo="Entrar" onPress={entrar} carregando={login.isPending} />

          <View className="flex-row items-center justify-center">
            <Text className="text-base text-ink-muted">Não tem conta? </Text>
            <Pressable
              onPress={() => abrirCadastro({ motivo, proximo }, true)}
              accessibilityRole="link"
              accessibilityLabel="Cadastre-se"
              hitSlop={12}
            >
              <Text className="text-base font-semibold text-primary">Cadastre-se</Text>
            </Pressable>
          </View>

          <View className="flex-row items-center gap-3">
            <View className="h-px flex-1 bg-border" />
            <Text className="text-sm text-ink-subtle">ou</Text>
            <View className="h-px flex-1 bg-border" />
          </View>

          <Button rotulo="Continuar sem login" variante="outline" onPress={fechar} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
