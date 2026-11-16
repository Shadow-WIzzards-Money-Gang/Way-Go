import * as Haptics from 'expo-haptics';
import { useLocalSearchParams } from 'expo-router';
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
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/TextField';
import { useRedefinirSenha, useSolicitarRecuperacaoSenha } from '@/hooks/queries';
import { concluirEntrada } from '@/lib/conta';

// Mesmas regras do backend (Usuario.validarSenha e RecuperacaoSenhaService).
const SENHA_MINIMO = 8;
const SENHA_MAXIMO = 72;
const TAMANHO_CODIGO = 6;
const ESPERA_REENVIO_SEGUNDOS = 60;

/** Recebe o código enviado por e-mail e a nova senha; ao concluir, já entra na conta. */
export default function RedefinirSenhaScreen() {
  const { email = '', proximo } = useLocalSearchParams<{ email?: string; proximo?: string }>();
  const redefinir = useRedefinirSenha();
  const reenviar = useSolicitarRecuperacaoSenha();
  const novaRef = useRef<TextInputInstance>(null);
  const confirmacaoRef = useRef<TextInputInstance>(null);

  const [codigo, setCodigo] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [tentou, setTentou] = useState(false);
  const [espera, setEspera] = useState(ESPERA_REENVIO_SEGUNDOS);

  useEffect(() => {
    if (espera <= 0) return;
    const timer = setTimeout(() => setEspera((s) => s - 1), 1000);
    return () => clearTimeout(timer);
  }, [espera]);

  const erroCodigo =
    codigo.length !== TAMANHO_CODIGO ? `Digite os ${TAMANHO_CODIGO} dígitos do código.` : null;
  const erroNova =
    novaSenha.length < SENHA_MINIMO
      ? `Use ao menos ${SENHA_MINIMO} caracteres.`
      : novaSenha.length > SENHA_MAXIMO
        ? `Use no máximo ${SENHA_MAXIMO} caracteres.`
        : !novaSenha.trim()
          ? 'A senha não pode ser só espaços.'
          : null;
  const erroConfirmacao = confirmacao !== novaSenha ? 'As senhas não conferem.' : null;

  const erro = redefinir.error;
  const erroApi = erro
    ? erro instanceof ApiError
      ? erro.message
      : 'Não foi possível redefinir a senha. Tente novamente.'
    : null;

  const salvar = () => {
    setTentou(true);
    if (erroCodigo || erroNova || erroConfirmacao || redefinir.isPending) return;
    redefinir.mutate(
      { email, codigo, novaSenha },
      {
        onSuccess: () => {
          void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
            () => undefined,
          );
          // Fecha esta tela e o login que está por baixo dela.
          concluirEntrada(proximo, 2);
        },
      },
    );
  };

  const pedirNovoCodigo = () => {
    if (espera > 0 || reenviar.isPending) return;
    reenviar.mutate(email, {
      onSuccess: () => {
        setEspera(ESPERA_REENVIO_SEGUNDOS);
        setCodigo('');
        redefinir.reset();
      },
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
            Se houver uma conta com <Text className="font-semibold text-ink">{email}</Text>, você
            vai receber um código de {TAMANHO_CODIGO} dígitos. Ele vale por 15 minutos.
          </Text>

          <TextField
            rotulo="Código"
            value={codigo}
            onChangeText={(t) => {
              setCodigo(t.replace(/\D/g, '').slice(0, TAMANHO_CODIGO));
              if (redefinir.error) redefinir.reset();
            }}
            placeholder="000000"
            keyboardType="number-pad"
            autoComplete="one-time-code"
            textContentType="oneTimeCode"
            maxLength={TAMANHO_CODIGO}
            returnKeyType="next"
            onSubmitEditing={() => novaRef.current?.focus()}
            submitBehavior="submit"
            erro={tentou ? erroCodigo : null}
          />
          <TextField
            ref={novaRef}
            rotulo="Nova senha"
            senha
            value={novaSenha}
            onChangeText={setNovaSenha}
            placeholder="Crie uma nova senha"
            autoCapitalize="none"
            autoComplete="new-password"
            textContentType="newPassword"
            returnKeyType="next"
            onSubmitEditing={() => confirmacaoRef.current?.focus()}
            submitBehavior="submit"
            dica={`Mínimo de ${SENHA_MINIMO} caracteres.`}
            erro={tentou ? erroNova : null}
          />
          <TextField
            ref={confirmacaoRef}
            rotulo="Confirmar nova senha"
            senha
            value={confirmacao}
            onChangeText={setConfirmacao}
            placeholder="Repita a nova senha"
            autoCapitalize="none"
            autoComplete="new-password"
            textContentType="newPassword"
            returnKeyType="go"
            onSubmitEditing={salvar}
            erro={tentou ? erroConfirmacao : null}
          />

          {erroApi && <AlertaErro mensagem={erroApi} />}

          <Button rotulo="Redefinir senha" onPress={salvar} carregando={redefinir.isPending} />

          {reenviar.error && (
            <AlertaErro
              mensagem={
                reenviar.error instanceof ApiError
                  ? reenviar.error.message
                  : 'Não foi possível reenviar o código. Tente novamente.'
              }
            />
          )}

          <View className="items-center gap-1">
            {reenviar.isSuccess && (
              <Text className="text-sm text-ink-muted">Enviamos um novo código.</Text>
            )}
            {espera > 0 ? (
              <Text className="text-base text-ink-subtle">
                Reenviar código em {espera}s
              </Text>
            ) : (
              <Pressable
                onPress={pedirNovoCodigo}
                disabled={reenviar.isPending}
                accessibilityRole="button"
                accessibilityLabel="Reenviar código"
                hitSlop={12}
              >
                <Text className="text-base font-semibold text-primary">
                  {reenviar.isPending ? 'Enviando...' : 'Reenviar código'}
                </Text>
              </Pressable>
            )}
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
