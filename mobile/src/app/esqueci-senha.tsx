import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/TextField';
import { useSolicitarRecuperacaoSenha } from '@/hooks/queries';
import { ehEmailValido } from '@/utils/text';

/** Aberta a partir do login: pede o código de recuperação e segue para a redefinição. */
export default function EsqueciSenhaScreen() {
  const params = useLocalSearchParams<{ email?: string; proximo?: string }>();
  const solicitar = useSolicitarRecuperacaoSenha();

  const [email, setEmail] = useState(params.email ?? '');
  const [tentou, setTentou] = useState(false);

  const erroEmail = !email.trim()
    ? 'Informe seu e-mail.'
    : !ehEmailValido(email)
      ? 'Confira o e-mail digitado.'
      : null;

  const erro = solicitar.error;
  const erroApi = erro
    ? erro instanceof ApiError
      ? erro.message
      : 'Não foi possível enviar o código. Tente novamente.'
    : null;

  const enviar = () => {
    setTentou(true);
    if (erroEmail || solicitar.isPending) return;
    solicitar.mutate(email, {
      // Troca esta tela pela de redefinição: voltar de lá leva direto ao login.
      onSuccess: () =>
        router.replace({
          pathname: '/redefinir-senha',
          params: { email: email.trim(), ...(params.proximo ? { proximo: params.proximo } : {}) },
        }),
    });
  };

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView
          contentContainerClassName="gap-5 p-4 pb-8"
          keyboardShouldPersistTaps="handled"
        >
          <Text className="text-base leading-6 text-ink-muted">
            Informe o e-mail da sua conta. Vamos enviar um código de 6 dígitos para você criar uma
            nova senha.
          </Text>

          <TextField
            rotulo="E-mail"
            value={email}
            onChangeText={(t) => {
              setEmail(t);
              if (solicitar.error) solicitar.reset();
            }}
            placeholder="voce@email.com"
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            autoComplete="email"
            textContentType="emailAddress"
            returnKeyType="send"
            onSubmitEditing={enviar}
            erro={tentou ? erroEmail : null}
          />

          {erroApi && <AlertaErro mensagem={erroApi} />}

          <Button rotulo="Enviar código" onPress={enviar} carregando={solicitar.isPending} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
