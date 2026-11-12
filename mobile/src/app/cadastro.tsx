import * as Haptics from 'expo-haptics';
import { router, useLocalSearchParams } from 'expo-router';
import { ChevronLeft } from 'lucide-react-native';
import { useRef, useState } from 'react';
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
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/TextField';
import { useRegistro } from '@/hooks/queries';
import { concluirEntrada, type MotivoLogin } from '@/lib/conta';
import { colors } from '@/theme/colors';
import { ehEmailValido } from '@/utils/text';

// Mesmas regras do backend (Usuario.validarSenha).
const SENHA_MINIMO = 8;
const SENHA_MAXIMO = 72;

export default function CadastroScreen() {
  const { motivo, proximo, deLogin } = useLocalSearchParams<{
    motivo?: MotivoLogin;
    proximo?: string;
    deLogin?: string;
  }>();
  const veioDoLogin = deLogin === '1';
  const registro = useRegistro();
  const senhaRef = useRef<TextInputInstance>(null);
  const confirmacaoRef = useRef<TextInputInstance>(null);

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [tentou, setTentou] = useState(false);

  const erroEmailLocal = !email.trim()
    ? 'Informe seu e-mail.'
    : !ehEmailValido(email)
      ? 'Confira o e-mail digitado.'
      : null;
  const erroSenha =
    senha.length < SENHA_MINIMO
      ? `Use ao menos ${SENHA_MINIMO} caracteres.`
      : senha.length > SENHA_MAXIMO
        ? `Use no máximo ${SENHA_MAXIMO} caracteres.`
        : !senha.trim()
          ? 'A senha não pode ser só espaços.'
          : null;
  const erroConfirmacao = confirmacao !== senha ? 'As senhas não conferem.' : null;

  // E-mail já cadastrado aparece junto do campo; os demais erros, numa faixa acima do botão.
  const erro = registro.error;
  const emailEmUso = erro instanceof ApiError && erro.tipo === 'conflito';
  const erroEmail = (tentou ? erroEmailLocal : null) ?? (emailEmUso ? erro.message : null);
  const erroApi =
    erro && !emailEmUso
      ? erro instanceof ApiError
        ? erro.message
        : 'Não foi possível criar a conta. Tente novamente.'
      : null;

  const criarConta = () => {
    setTentou(true);
    if (erroEmailLocal || erroSenha || erroConfirmacao || registro.isPending) return;
    registro.mutate(
      { email, senha },
      {
        onSuccess: () => {
          void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
            () => undefined,
          );
          // Fecha o cadastro e o login que está por baixo dele (se houver).
          concluirEntrada(proximo, veioDoLogin ? 2 : 1);
        },
      },
    );
  };

  /** Volta ao login se ele estiver por baixo; senão troca o cadastro pelo login. */
  const irParaLogin = () => {
    if (veioDoLogin) {
      router.back();
      return;
    }
    router.replace({
      pathname: '/login',
      params: { ...(motivo ? { motivo } : {}), ...(proximo ? { proximo } : {}) },
    });
  };

  return (
    <SafeAreaView className="flex-1 bg-background">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView
          contentContainerClassName="flex-grow gap-5 px-5 pb-8 pt-2"
          keyboardShouldPersistTaps="handled"
        >
          <Pressable
            onPress={() => router.back()}
            accessibilityRole="button"
            accessibilityLabel="Voltar"
            className="-ml-2 h-11 w-11 items-center justify-center rounded-full active:bg-neutral-soft"
          >
            <ChevronLeft size={26} color={colors.ink.DEFAULT} />
          </Pressable>

          <View>
            <Text className="text-4xl font-bold text-ink" accessibilityRole="header">
              Criar conta
            </Text>
            <Text className="mt-2 text-base leading-6 text-ink-muted">
              Com uma conta você salva rotas e recebe alertas no celular.
            </Text>
          </View>

          <TextField
            rotulo="E-mail"
            value={email}
            onChangeText={(t) => {
              setEmail(t);
              if (emailEmUso) registro.reset();
            }}
            placeholder="voce@email.com"
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            autoComplete="email"
            textContentType="emailAddress"
            returnKeyType="next"
            onSubmitEditing={() => senhaRef.current?.focus()}
            submitBehavior="submit"
            erro={erroEmail}
          />
          <TextField
            ref={senhaRef}
            rotulo="Senha"
            senha
            value={senha}
            onChangeText={setSenha}
            placeholder="Crie uma senha"
            autoCapitalize="none"
            autoComplete="new-password"
            textContentType="newPassword"
            returnKeyType="next"
            onSubmitEditing={() => confirmacaoRef.current?.focus()}
            submitBehavior="submit"
            dica={`Mínimo de ${SENHA_MINIMO} caracteres.`}
            erro={tentou ? erroSenha : null}
          />
          <TextField
            ref={confirmacaoRef}
            rotulo="Confirmar senha"
            senha
            value={confirmacao}
            onChangeText={setConfirmacao}
            placeholder="Repita a senha"
            autoCapitalize="none"
            autoComplete="new-password"
            textContentType="newPassword"
            returnKeyType="go"
            onSubmitEditing={criarConta}
            erro={tentou ? erroConfirmacao : null}
          />

          {erroApi && <AlertaErro mensagem={erroApi} />}

          <Button rotulo="Criar conta" onPress={criarConta} carregando={registro.isPending} />

          <View className="flex-row items-center justify-center">
            <Text className="text-base text-ink-muted">Já tem conta? </Text>
            <Pressable
              onPress={irParaLogin}
              accessibilityRole="link"
              accessibilityLabel="Entrar"
              hitSlop={12}
            >
              <Text className="text-base font-semibold text-primary">Entrar</Text>
            </Pressable>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
