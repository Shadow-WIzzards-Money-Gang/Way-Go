import * as Haptics from 'expo-haptics';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Alert, Text, View } from 'react-native';

import { FormularioEstacao } from '@/components/admin/FormularioEstacao';
import { LinhaBadge } from '@/components/LinhaBadge';
import { Skeleton } from '@/components/ui/Skeleton';
import { ErrorState } from '@/components/ui/States';
import { useAtualizarEstacao, useDesativarEstacao, useEstacao } from '@/hooks/queries';
import { mensagemDeErro } from '@/utils/admin';
import { compararCodigos } from '@/utils/text';

const sucesso = () =>
  void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(() => undefined);

export default function EstacaoAdminScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { data: estacao, isPending, error, refetch } = useEstacao(Number(id));
  const atualizar = useAtualizarEstacao();
  const desativar = useDesativarEstacao();
  const [erroApi, setErroApi] = useState<string | null>(null);

  if (isPending) {
    return (
      <View className="gap-4 bg-background p-4">
        <Skeleton className="h-24" />
        <Skeleton className="h-72" />
      </View>
    );
  }
  if (error || !estacao) {
    return <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />;
  }

  const confirmarDesativacao = () =>
    Alert.alert(
      'Desativar estação?',
      `${estacao.nome} deixa de ser usada no cálculo de rotas. Você pode reativá-la depois.`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Desativar',
          style: 'destructive',
          onPress: () =>
            desativar.mutate(estacao.id, {
              onSuccess: () => {
                sucesso();
                router.back();
              },
              onError: (erro) =>
                setErroApi(mensagemDeErro(erro, 'Não foi possível desativar a estação.')),
            }),
        },
      ],
    );

  const linhas = [...estacao.linhas].sort((a, b) => compararCodigos(a.codigo, b.codigo));

  return (
    <>
      <Stack.Screen options={{ title: estacao.nome }} />
      <FormularioEstacao
        // Remonta com os valores do servidor depois de salvar.
        key={`${estacao.nome}|${estacao.latitude}|${estacao.longitude}|${estacao.ativa}`}
        existente={estacao}
        linhas={
          <View className="flex-row flex-wrap items-center gap-1.5">
            <Text className="text-sm text-ink-muted">
              {linhas.length === 0 ? 'Nenhuma linha passa por aqui.' : 'Linhas:'}
            </Text>
            {linhas.map((l) => (
              <LinhaBadge key={l.id} codigo={l.codigo} cor={l.cor} tamanho="sm" />
            ))}
          </View>
        }
        salvando={atualizar.isPending}
        erroApi={erroApi}
        aoSalvar={(dados) => {
          setErroApi(null);
          atualizar.mutate(
            { id: estacao.id, dados },
            {
              onSuccess: () => {
                sucesso();
                router.back();
              },
              onError: (erro) =>
                setErroApi(mensagemDeErro(erro, 'Não foi possível salvar a estação.')),
            },
          );
        }}
        aoDesativar={confirmarDesativacao}
        desativando={desativar.isPending}
      />
    </>
  );
}
