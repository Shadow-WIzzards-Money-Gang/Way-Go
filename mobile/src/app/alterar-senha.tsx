import * as Haptics from 'expo-haptics';
import { router } from 'expo-router';
import { useRef, useState } from 'react';
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Text,
  type TextInputInstance,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/TextField';
import { useAlterarSenha } from '@/hooks/queries';

// Mesmas regras do backend (Usuario.validarSenha).
const SENHA_MINIMO = 8;
const SENHA_MAXIMO = 72;

export default function AlterarSenhaScreen() {
  const alterarSenha = useAlterarSenha();
  const novaRef = useRef<TextInputInstance>(null);
  const confirmacaoRef = useRef<TextInputInstance>(null);

  const [senhaAtual, setSenhaAtual] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [tentou, setTentou] = useState(false);

  const erroAtual = !senhaAtual ? 'Informe sua senha atual.' : null;
  const erroNova =
    novaSenha.length < SENHA_MINIMO
      ? `Use ao menos ${SENHA_MINIMO} caracteres.`
      : novaSenha.length > SENHA_MAXIMO
        ? `Use no máximo ${SENHA_MAXIMO} caracteres.`
        : !novaSenha.trim()
          ? 'A senha não pode ser só espaços.'
          : novaSenha === senhaAtual
            ? 'A nova senha deve ser diferente da atual.'
            : null;
  const erroConfirmacao = confirmacao !== novaSenha ? 'As senhas não conferem.' : null;

  const erro = alterarSenha.error;
  const erroApi = erro
    ? erro instanceof ApiError
      ? erro.message
      : 'Não foi possível alterar a senha. Tente novamente.'
    : null;

  const salvar = () => {
    setTentou(true);
    if (erroAtual || erroNova || erroConfirmacao || alterarSenha.isPending) return;
    alterarSenha.mutate(
      { senhaAtual, novaSenha },
      {
        onSuccess: () => {
          void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
            () => undefined,
          );
          Alert.alert('Senha alterada', 'Sua senha foi atualizada com sucesso.');
          router.back();
        },
      },
    );
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
            Confirme sua senha atual e escolha uma nova.
          </Text>

          <TextField
            rotulo="Senha atual"
            senha
            value={senhaAtual}
            onChangeText={(t) => {
              setSenhaAtual(t);
              if (alterarSenha.error) alterarSenha.reset();
            }}
            placeholder="Sua senha atual"
            autoCapitalize="none"
            autoComplete="current-password"
            textContentType="password"
            returnKeyType="next"
            onSubmitEditing={() => novaRef.current?.focus()}
            submitBehavior="submit"
            erro={tentou ? erroAtual : null}
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

          <Button rotulo="Salvar nova senha" onPress={salvar} carregando={alterarSenha.isPending} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
