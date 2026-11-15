import { TriangleAlert } from 'lucide-react-native';
import { useState } from 'react';
import { KeyboardAvoidingView, Modal, Platform, Pressable, Text, View } from 'react-native';

import { useExcluirConta } from '@/hooks/queries';
import { colors } from '@/theme/colors';
import { mensagemDeErro } from '@/utils/admin';
import { confirmacaoExclusaoValida, FRASE_EXCLUIR_CONTA } from '@/utils/text';

import { Button } from './ui/Button';
import { TextField } from './ui/TextField';

interface ConfirmarExclusaoContaProps {
  visivel: boolean;
  aoFechar: () => void;
  /** Chamado depois que a API confirmou a exclusão. */
  aoExcluir: () => void;
}

/** Pede que o usuário digite "deletar conta" antes de excluir a conta de vez. */
export function ConfirmarExclusaoConta({
  visivel,
  aoFechar,
  aoExcluir,
}: ConfirmarExclusaoContaProps) {
  const [texto, setTexto] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const excluir = useExcluirConta();

  const fechar = () => {
    if (excluir.isPending) return;
    setTexto('');
    setErro(null);
    aoFechar();
  };

  const confirmar = () => {
    setErro(null);
    excluir.mutate(undefined, {
      onSuccess: () => {
        setTexto('');
        aoExcluir();
      },
      onError: (e) =>
        setErro(mensagemDeErro(e, 'Não foi possível excluir a conta. Tente novamente.')),
    });
  };

  return (
    <Modal
      visible={visivel}
      transparent
      animationType="fade"
      onRequestClose={fechar}
      statusBarTranslucent
    >
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <Pressable
          onPress={fechar}
          accessibilityRole="button"
          accessibilityLabel="Fechar confirmação"
          className="flex-1 items-center justify-center bg-ink/30 p-4"
        >
          <View
            className="w-full max-w-md gap-4 rounded-2xl border border-border bg-surface p-5 shadow-lg"
            style={{ elevation: 8 }}
            // Toques dentro do card não fecham o modal.
            onStartShouldSetResponder={() => true}
            accessibilityViewIsModal
          >
            <View className="flex-row items-center gap-3">
              <View className="h-10 w-10 items-center justify-center rounded-full bg-danger-soft">
                <TriangleAlert size={20} color={colors.danger.DEFAULT} />
              </View>
              <Text className="flex-1 text-lg font-semibold text-ink">Excluir conta?</Text>
            </View>

            <Text className="text-sm text-ink-muted">
              Sua conta, suas rotas salvas e seus agendamentos serão apagados para sempre. Essa ação
              não pode ser desfeita.
            </Text>

            <TextField
              rotulo={`Digite "${FRASE_EXCLUIR_CONTA}" para confirmar`}
              placeholder={FRASE_EXCLUIR_CONTA}
              value={texto}
              onChangeText={setTexto}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="off"
              editable={!excluir.isPending}
              erro={erro}
              returnKeyType="done"
            />

            <View className="gap-2">
              <Button
                rotulo="Excluir conta"
                variante="danger"
                onPress={confirmar}
                desabilitado={!confirmacaoExclusaoValida(texto)}
                carregando={excluir.isPending}
              />
              <Button
                rotulo="Cancelar"
                variante="outline"
                onPress={fechar}
                desabilitado={excluir.isPending}
              />
            </View>
          </View>
        </Pressable>
      </KeyboardAvoidingView>
    </Modal>
  );
}
