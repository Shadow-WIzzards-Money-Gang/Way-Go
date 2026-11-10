import { router, Stack, useLocalSearchParams } from 'expo-router';
import { useMemo, useState } from 'react';
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { LinhaBadge } from '@/components/LinhaBadge';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Chip } from '@/components/ui/Chip';
import { useRota } from '@/hooks/queries';
import { agendarLembretes } from '@/services/notifications';
import { type RotaSalva, useRotasSalvas } from '@/store/rotasSalvas';
import { colors } from '@/theme/colors';
import type { CriterioRota, LinhaDaEstacao } from '@/types/api';
import { DIAS_DA_SEMANA, ehHorarioValido } from '@/utils/format';

type Params = {
  id?: string;
  origemId?: string;
  origemNome?: string;
  destinoId?: string;
  destinoNome?: string;
  criterio?: CriterioRota;
  agendar?: string;
};

const DIAS_UTEIS = [1, 2, 3, 4, 5];

/** "0710" -> "07:10" enquanto o usuário digita. */
function mascararHorario(texto: string): string {
  const digitos = texto.replace(/\D/g, '').slice(0, 4);
  return digitos.length <= 2 ? digitos : `${digitos.slice(0, 2)}:${digitos.slice(2)}`;
}

export default function RotaSalvaScreen() {
  const params = useLocalSearchParams<Params>();
  const existente = useRotasSalvas((s) => s.rotas.find((r) => r.id === params.id));
  const adicionar = useRotasSalvas((s) => s.adicionar);
  const atualizar = useRotasSalvas((s) => s.atualizar);

  const origem =
    existente?.origem ??
    (params.origemId && params.origemNome
      ? { id: Number(params.origemId), nome: params.origemNome }
      : null);
  const destino =
    existente?.destino ??
    (params.destinoId && params.destinoNome
      ? { id: Number(params.destinoId), nome: params.destinoNome }
      : null);
  const criterio: CriterioRota = existente?.criterio ?? params.criterio ?? 'EQUILIBRADO';

  // Para rota nova, as linhas vêm do cálculo que já está em cache (feito no Mapa).
  const { data: rota } = useRota(
    !existente && origem && destino
      ? { origem: origem.nome, destino: destino.nome, criterio }
      : null,
  );
  const linhas: LinhaDaEstacao[] = useMemo(() => {
    if (existente) return existente.linhas;
    const unicas = new Map<number, LinhaDaEstacao>();
    for (const t of rota?.trechos ?? []) {
      const { id, nome, codigo, cor } = t.linha;
      unicas.set(id, { id, nome, codigo, cor });
    }
    return [...unicas.values()];
  }, [existente, rota]);

  const [nome, setNome] = useState(existente?.nome ?? '');
  const [agendar, setAgendar] = useState(Boolean(existente?.agendamento) || params.agendar === '1');
  const [dias, setDias] = useState<number[]>(existente?.agendamento?.dias ?? DIAS_UTEIS);
  const [horario, setHorario] = useState(existente?.agendamento?.horario ?? '07:00');
  const [salvando, setSalvando] = useState(false);
  const [tentouSalvar, setTentouSalvar] = useState(false);

  if (!origem || !destino) {
    return (
      <SafeAreaView className="flex-1 items-center justify-center bg-background p-6">
        <Text className="text-center text-ink-muted">Rota não encontrada.</Text>
        <Button rotulo="Voltar" variante="outline" onPress={() => router.back()} className="mt-4" />
      </SafeAreaView>
    );
  }

  const nomePadrao = `${origem.nome} → ${destino.nome}`;
  const horarioInvalido = agendar && !ehHorarioValido(horario);
  const semDias = agendar && dias.length === 0;

  const alternarDia = (dia: number) =>
    setDias((atual) => (atual.includes(dia) ? atual.filter((d) => d !== dia) : [...atual, dia]));

  const salvar = async () => {
    setTentouSalvar(true);
    if (horarioInvalido || semDias) return;

    setSalvando(true);
    const dados = {
      nome: nome.trim() || nomePadrao,
      origem,
      destino,
      criterio,
      linhas,
      agendamento: agendar ? { dias: [...dias].sort((a, b) => a - b), horario } : null,
    };

    let salva: RotaSalva;
    if (existente) {
      atualizar(existente.id, dados);
      salva = { ...existente, ...dados };
    } else {
      salva = adicionar(dados);
    }

    const permitido = await agendarLembretes(salva).catch(() => false);
    setSalvando(false);
    if (agendar && !permitido) {
      Alert.alert(
        'Notificações desativadas',
        'A rota foi salva, mas sem permissão de notificação não conseguimos lembrar você no horário.',
      );
    }

    router.back();
    if (!existente) router.navigate('/rotas');
  };

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <Stack.Screen options={{ title: existente ? 'Editar rota' : 'Salvar rota' }} />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView contentContainerClassName="gap-4 p-4" keyboardShouldPersistTaps="handled">
          <Card>
            <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
              Trajeto
            </Text>
            <Text className="mt-1 text-base font-semibold text-ink">{nomePadrao}</Text>
            {linhas.length > 0 && (
              <View className="mt-2 flex-row flex-wrap gap-1.5">
                {linhas.map((l) => (
                  <LinhaBadge key={l.id} codigo={l.codigo} cor={l.cor} tamanho="sm" />
                ))}
              </View>
            )}
          </Card>

          <View>
            <Text className="mb-1.5 text-sm font-medium text-ink">Nome da rota</Text>
            <TextInput
              value={nome}
              onChangeText={setNome}
              placeholder={nomePadrao}
              placeholderTextColor={colors.ink.subtle}
              maxLength={40}
              returnKeyType="done"
              accessibilityLabel="Nome da rota"
              className="min-h-[48px] rounded-xl border border-border bg-surface px-3 text-base text-ink"
            />
            <Text className="mt-1 text-xs text-ink-subtle">Ex.: Casa → Trabalho, Faculdade</Text>
          </View>

          <Card>
            <View className="flex-row items-center justify-between gap-3">
              <View className="flex-1">
                <Text className="text-base font-semibold text-ink">Agendar lembrete</Text>
                <Text className="text-sm text-ink-muted">
                  Avisamos no horário de sair para você conferir o status.
                </Text>
              </View>
              <Switch
                value={agendar}
                onValueChange={setAgendar}
                trackColor={{ true: colors.primary.DEFAULT, false: colors.border }}
                accessibilityLabel="Agendar lembrete"
              />
            </View>

            {agendar && (
              <View className="mt-4 gap-4">
                <View>
                  <Text className="mb-2 text-sm font-medium text-ink">Dias</Text>
                  <View className="flex-row flex-wrap gap-2">
                    {DIAS_DA_SEMANA.map((d) => (
                      <Chip
                        key={d.indice}
                        rotulo={d.rotulo}
                        selecionado={dias.includes(d.indice)}
                        onPress={() => alternarDia(d.indice)}
                      />
                    ))}
                  </View>
                  {tentouSalvar && semDias && (
                    <Text className="mt-1.5 text-xs text-danger">Escolha ao menos um dia.</Text>
                  )}
                </View>
                <View>
                  <Text className="mb-1.5 text-sm font-medium text-ink">Horário</Text>
                  <TextInput
                    value={horario}
                    onChangeText={(t) => setHorario(mascararHorario(t))}
                    keyboardType="number-pad"
                    placeholder="07:10"
                    placeholderTextColor={colors.ink.subtle}
                    maxLength={5}
                    accessibilityLabel="Horário no formato horas e minutos"
                    className={`min-h-[48px] w-28 rounded-xl border bg-surface px-3 text-base text-ink ${
                      tentouSalvar && horarioInvalido ? 'border-danger' : 'border-border'
                    }`}
                  />
                  {tentouSalvar && horarioInvalido && (
                    <Text className="mt-1.5 text-xs text-danger">
                      Use o formato HH:mm, ex.: 07:10.
                    </Text>
                  )}
                </View>
              </View>
            )}
          </Card>
        </ScrollView>

        <View className="border-t border-border bg-surface p-4">
          <Button
            rotulo={existente ? 'Salvar alterações' : 'Salvar rota'}
            onPress={() => void salvar()}
            carregando={salvando}
          />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
