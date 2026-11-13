import * as Haptics from 'expo-haptics';
import { router } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { LinhaBadge } from '@/components/LinhaBadge';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Chip } from '@/components/ui/Chip';
import { TextField } from '@/components/ui/TextField';
import { useCriarLinha } from '@/hooks/queries';
import type { TipoTransporte } from '@/types/api';
import { mensagemDeErro, normalizarCor, TIPOS_TRANSPORTE } from '@/utils/admin';

export default function NovaLinhaScreen() {
  const criar = useCriarLinha();
  const [codigo, setCodigo] = useState('');
  const [nome, setNome] = useState('');
  const [cor, setCor] = useState('');
  const [operadora, setOperadora] = useState('');
  const [tipo, setTipo] = useState<TipoTransporte>('METRO');
  const [tentou, setTentou] = useState(false);
  const [erroApi, setErroApi] = useState<string | null>(null);

  const corNormalizada = normalizarCor(cor);
  const erros = {
    codigo: codigo.trim() ? null : 'Informe o código (ex.: 6).',
    nome: nome.trim() ? null : 'Informe o nome da linha.',
    operadora: operadora.trim() ? null : 'Informe a operadora.',
    cor: corNormalizada === null ? 'Use o formato #RRGGBB, ex.: #F28C00.' : null,
  };
  const valido = Object.values(erros).every((e) => e === null);

  const salvar = () => {
    setTentou(true);
    setErroApi(null);
    if (!valido) return;
    criar.mutate(
      {
        codigo: codigo.trim(),
        nome: nome.trim(),
        operadora: operadora.trim(),
        cor: corNormalizada || undefined,
        tipoTransporte: tipo,
      },
      {
        onSuccess: (linha) => {
          void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(
            () => undefined,
          );
          router.replace({ pathname: '/admin/linhas/[id]', params: { id: String(linha.id) } });
        },
        onError: (erro) => setErroApi(mensagemDeErro(erro, 'Não foi possível criar a linha.')),
      },
    );
  };

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView contentContainerClassName="gap-4 p-4" keyboardShouldPersistTaps="handled">
          <Card className="flex-row items-center gap-3">
            <LinhaBadge codigo={codigo.trim() || '?'} cor={corNormalizada || '#94A3B8'} />
            <View className="flex-1">
              <Text className="text-base font-semibold text-ink" numberOfLines={1}>
                {nome.trim() || 'Nova linha'}
              </Text>
              <Text className="text-sm text-ink-muted" numberOfLines={1}>
                {operadora.trim() || 'Operadora'}
              </Text>
            </View>
          </Card>

          <Card className="gap-4">
            <View className="flex-row gap-3">
              <TextField
                rotulo="Código"
                value={codigo}
                onChangeText={setCodigo}
                placeholder="6"
                autoCapitalize="none"
                maxLength={20}
                erro={tentou ? erros.codigo : null}
                className="w-28"
              />
              <TextField
                rotulo="Cor"
                value={cor}
                onChangeText={setCor}
                placeholder="#F28C00"
                autoCapitalize="characters"
                maxLength={7}
                erro={tentou ? erros.cor : null}
                className="flex-1"
              />
            </View>
            <TextField
              rotulo="Nome"
              value={nome}
              onChangeText={setNome}
              placeholder="Linha 6-Laranja"
              maxLength={100}
              erro={tentou ? erros.nome : null}
            />
            <TextField
              rotulo="Operadora"
              value={operadora}
              onChangeText={setOperadora}
              placeholder="Linha Uni"
              maxLength={100}
              erro={tentou ? erros.operadora : null}
            />
            <View>
              <Text className="mb-2 text-sm font-medium text-ink">Tipo de transporte</Text>
              <View className="flex-row flex-wrap gap-2">
                {TIPOS_TRANSPORTE.map((t) => (
                  <Chip
                    key={t.valor}
                    rotulo={t.rotulo}
                    selecionado={tipo === t.valor}
                    onPress={() => setTipo(t.valor)}
                  />
                ))}
              </View>
              <Text className="mt-2 text-xs text-ink-muted">
                Código e tipo não podem ser alterados depois.
              </Text>
            </View>
          </Card>

          {erroApi && <AlertaErro mensagem={erroApi} />}
        </ScrollView>
        <View className="border-t border-border bg-surface p-4">
          <Button rotulo="Criar linha" onPress={salvar} carregando={criar.isPending} />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
