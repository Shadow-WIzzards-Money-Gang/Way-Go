import * as Haptics from 'expo-haptics';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { BellRing, CalendarClock, Trash2 } from 'lucide-react-native';
import { useState } from 'react';
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Switch,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { ConviteLogin } from '@/components/ConviteLogin';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Chip } from '@/components/ui/Chip';
import { Skeleton } from '@/components/ui/Skeleton';
import { TextField } from '@/components/ui/TextField';
import {
  useAtualizarAgendamento,
  useCriarAgendamento,
  useExcluirAgendamento,
} from '@/hooks/queries';
import { type RotaDoUsuario, useRotasDoUsuario } from '@/hooks/useRotasDoUsuario';
import { hrefComParams } from '@/lib/conta';
import { pedirPermissaoNotificacoes } from '@/services/notifications';
import { useLogado } from '@/store/sessao';
import { colors } from '@/theme/colors';
import type { RotaAgendada } from '@/types/api';
import {
  diaParaIndice,
  indicesParaDias,
  mascararHorario,
  momentoDoLembrete,
} from '@/utils/agendamento';
import { DIAS_DA_SEMANA, ehHorarioValido, formatarDias } from '@/utils/format';

type Params = { idRota?: string; id?: string };

const DIAS_UTEIS = [1, 2, 3, 4, 5];
const ATALHOS_DIAS: { rotulo: string; dias: number[] }[] = [
  { rotulo: 'Dias úteis', dias: DIAS_UTEIS },
  { rotulo: 'Fim de semana', dias: [0, 6] },
  { rotulo: 'Todos os dias', dias: [0, 1, 2, 3, 4, 5, 6] },
];
const ANTECEDENCIAS = [0, 5, 10, 15, 30];

const mesmosDias = (a: number[], b: number[]) =>
  a.length === b.length && [...a].sort().join() === [...b].sort().join();

/** Modal para criar (params: idRota) ou editar (params: idRota, id) um agendamento da rota. */
export default function AgendamentoScreen() {
  const params = useLocalSearchParams<Params>();
  const logado = useLogado();
  const { rotas, isPending, agendamentosCarregando } = useRotasDoUsuario();

  const rota = rotas.find((r) => r.id === Number(params.idRota));
  const existente = params.id
    ? rota?.agendamentos.find((a) => a.id === Number(params.id))
    : undefined;
  const titulo = params.id ? 'Editar horário' : 'Novo horário';

  if (!logado) {
    return (
      <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
        <Stack.Screen options={{ title: titulo }} />
        <ConviteLogin
          icone={CalendarClock}
          titulo="Entre para agendar suas rotas"
          mensagem="Os horários ficam guardados na sua conta e os lembretes chegam no seu celular."
          parametros={{ motivo: 'rotas', proximo: hrefComParams('/agendamento', params) }}
        />
      </SafeAreaView>
    );
  }

  const carregando = isPending || (params.id !== undefined && agendamentosCarregando);
  if (!rota || (params.id && !existente)) {
    return (
      <SafeAreaView edges={['bottom']} className="flex-1 bg-background p-4">
        <Stack.Screen options={{ title: titulo }} />
        {carregando ? (
          <View className="gap-4">
            <Skeleton className="h-20" />
            <Skeleton className="h-32" />
            <Skeleton className="h-24" />
          </View>
        ) : (
          <View className="flex-1 items-center justify-center">
            <Text className="text-center text-ink-muted">
              {rota ? 'Horário não encontrado.' : 'Rota não encontrada.'}
            </Text>
            <Button
              rotulo="Voltar"
              variante="outline"
              onPress={() => router.back()}
              className="mt-4"
            />
          </View>
        )}
      </SafeAreaView>
    );
  }

  return (
    <FormularioAgendamento
      // Remonta se o agendamento editado mudar, para o estado inicial valer.
      key={existente?.id ?? 'novo'}
      rota={rota}
      existente={existente}
      titulo={titulo}
    />
  );
}

function FormularioAgendamento({
  rota,
  existente,
  titulo,
}: {
  rota: RotaDoUsuario;
  existente: RotaAgendada | undefined;
  titulo: string;
}) {
  const criar = useCriarAgendamento();
  const atualizar = useAtualizarAgendamento();
  const excluir = useExcluirAgendamento();

  const [dias, setDias] = useState<number[]>(
    existente ? existente.diasSemana.map(diaParaIndice) : DIAS_UTEIS,
  );
  const [horario, setHorario] = useState(existente?.horario ?? '07:00');
  const [antecedencia, setAntecedencia] = useState(existente?.antecedenciaMinutos ?? 5);
  const [ativa, setAtiva] = useState(existente?.ativa ?? true);
  const [salvando, setSalvando] = useState(false);
  const [tentouSalvar, setTentouSalvar] = useState(false);
  const [erroApi, setErroApi] = useState<string | null>(null);

  const horarioValido = ehHorarioValido(horario);
  const semDias = dias.length === 0;
  const opcoesAntecedencia = ANTECEDENCIAS.includes(antecedencia)
    ? ANTECEDENCIAS
    : [...ANTECEDENCIAS, antecedencia].sort((a, b) => a - b);

  const alternarDia = (dia: number) =>
    setDias((atual) => (atual.includes(dia) ? atual.filter((d) => d !== dia) : [...atual, dia]));

  // Prévia "Lembrete às 06:55" para deixar claro o efeito da antecedência.
  const previa = (() => {
    if (!horarioValido || semDias) return null;
    const [h, m] = horario.split(':').map(Number);
    const momento = momentoDoLembrete(dias[0], h * 60 + m, antecedencia);
    const hora = `${String(momento.hora).padStart(2, '0')}:${String(momento.minuto).padStart(2, '0')}`;
    const mudaDeDia = momento.dia !== dias[0];
    return `Lembrete às ${hora}${mudaDeDia ? ' do dia anterior' : ''} · ${formatarDias(dias)}`;
  })();

  const tratarErro = (erro: unknown, padrao: string) => {
    // 401 já abre o login; os demais erros aparecem no formulário.
    if (erro instanceof ApiError && erro.tipo === 'nao-autorizado') return;
    setErroApi(erro instanceof ApiError ? erro.message : padrao);
  };

  const salvar = async () => {
    setTentouSalvar(true);
    setErroApi(null);
    if (!horarioValido || semDias) return;

    setSalvando(true);
    const dados = {
      horario,
      antecedenciaMinutos: antecedencia,
      diasSemana: indicesParaDias(dias),
      ativa,
    };
    try {
      if (existente) await atualizar.mutateAsync({ id: existente.id, dados });
      else await criar.mutateAsync({ idRota: rota.id, dados });
    } catch (erro) {
      setSalvando(false);
      tratarErro(erro, 'Não foi possível salvar o horário. Tente novamente.');
      return;
    }

    // Os lembretes do aparelho são recriados pelo useLembretesAgendamentos; aqui só a permissão.
    const permitido = ativa ? await pedirPermissaoNotificacoes().catch(() => false) : true;
    setSalvando(false);
    void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(() => undefined);
    if (!permitido) {
      Alert.alert(
        'Notificações desativadas',
        'O horário foi salvo, mas sem permissão de notificação não conseguimos lembrar você.',
      );
    }
    router.back();
  };

  const confirmarExclusao = () => {
    if (!existente) return;
    Alert.alert('Excluir horário?', `O lembrete de ${existente.horario} deixará de ser enviado.`, [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Excluir',
        style: 'destructive',
        onPress: () =>
          excluir.mutate(existente.id, {
            onSuccess: () => router.back(),
            onError: (erro) => tratarErro(erro, 'Não foi possível excluir. Tente novamente.'),
          }),
      },
    ]);
  };

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <Stack.Screen options={{ title: titulo }} />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView contentContainerClassName="gap-4 p-4" keyboardShouldPersistTaps="handled">
          <Card>
            <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
              Rota
            </Text>
            <Text className="mt-1 text-base font-semibold text-ink" numberOfLines={1}>
              {rota.nome}
            </Text>
            <Text className="text-sm text-ink-muted" numberOfLines={1}>
              {rota.origem.nome} → {rota.destino.nome}
            </Text>
          </Card>

          <Card className="gap-4">
            <TextField
              rotulo="Horário de saída"
              value={horario}
              onChangeText={(t) => setHorario(mascararHorario(t))}
              keyboardType="number-pad"
              placeholder="07:10"
              maxLength={5}
              erro={tentouSalvar && !horarioValido ? 'Use o formato HH:mm, ex.: 07:10.' : null}
              className="w-36"
            />

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
              <View className="mt-2 flex-row flex-wrap gap-x-4">
                {ATALHOS_DIAS.map((atalho) => (
                  <Button
                    key={atalho.rotulo}
                    rotulo={atalho.rotulo}
                    variante="ghost"
                    compacto
                    desabilitado={mesmosDias(dias, atalho.dias)}
                    onPress={() => setDias(atalho.dias)}
                  />
                ))}
              </View>
              {tentouSalvar && semDias && (
                <Text className="mt-1.5 text-xs text-danger">Escolha ao menos um dia.</Text>
              )}
            </View>
          </Card>

          <Card className="gap-3">
            <View>
              <Text className="text-base font-semibold text-ink">Lembrete</Text>
              <Text className="text-sm text-ink-muted">
                Quanto tempo antes da saída você quer ser avisado.
              </Text>
            </View>
            <View className="flex-row flex-wrap gap-2">
              {opcoesAntecedencia.map((min) => (
                <Chip
                  key={min}
                  rotulo={min === 0 ? 'Na hora' : `${min} min antes`}
                  selecionado={antecedencia === min}
                  onPress={() => setAntecedencia(min)}
                />
              ))}
            </View>
            {previa && ativa && (
              <View className="flex-row items-center gap-2 rounded-xl bg-primary-soft px-3 py-2.5">
                <BellRing size={16} color={colors.primary.DEFAULT} />
                <Text className="flex-1 text-sm text-primary" accessibilityLiveRegion="polite">
                  {previa}
                </Text>
              </View>
            )}
          </Card>

          <Card className="flex-row items-center gap-3">
            <View className="flex-1">
              <Text className="text-base font-semibold text-ink">Ativo</Text>
              <Text className="text-sm text-ink-muted">
                {ativa ? 'Você recebe os lembretes.' : 'Pausado: nenhum lembrete é enviado.'}
              </Text>
            </View>
            <Switch
              value={ativa}
              onValueChange={setAtiva}
              trackColor={{ true: colors.primary.DEFAULT, false: colors.border }}
              accessibilityLabel="Agendamento ativo"
            />
          </Card>

          {erroApi && <AlertaErro mensagem={erroApi} />}

          {existente && (
            <Button
              rotulo="Excluir horário"
              variante="danger"
              icone={Trash2}
              carregando={excluir.isPending}
              onPress={confirmarExclusao}
            />
          )}
        </ScrollView>

        <View className="border-t border-border bg-surface p-4">
          <Button
            rotulo={existente ? 'Salvar alterações' : 'Adicionar horário'}
            onPress={() => void salvar()}
            carregando={salvando}
          />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
