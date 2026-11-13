import * as Haptics from 'expo-haptics';
import { router } from 'expo-router';
import { useState } from 'react';

import { FormularioEstacao } from '@/components/admin/FormularioEstacao';
import { useCriarEstacao } from '@/hooks/queries';
import { mensagemDeErro } from '@/utils/admin';

export default function NovaEstacaoScreen() {
  const criar = useCriarEstacao();
  const [erroApi, setErroApi] = useState<string | null>(null);

  return (
    <FormularioEstacao
      salvando={criar.isPending}
      erroApi={erroApi}
      aoSalvar={(dados) => {
        setErroApi(null);
        criar.mutate(dados, {
          onSuccess: () => {
            void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
              () => undefined,
            );
            router.back();
          },
          onError: (erro) => setErroApi(mensagemDeErro(erro, 'Não foi possível criar a estação.')),
        });
      }}
    />
  );
}
