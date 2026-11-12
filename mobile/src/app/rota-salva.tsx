import * as Haptics from 'expo-haptics';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { Bookmark } from 'lucide-react-native';
import { useState } from 'react';
import { Alert, KeyboardAvoidingView, Platform, ScrollView, Switch, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { ConviteLogin } from '@/components/ConviteLogin';
import { LinhaBadge } from '@/components/LinhaBadge';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Chip } from '@/components/ui/Chip';
import { Skeleton } from '@/components/ui/Skeleton';
import { TextField } from '@/components/ui/TextField';
import { useAtualizarRota, useRota, useSalvarRota } from '@/hooks/queries';
import { type RotaDoUsuario, useRotasDoUsuario } from '@/hooks/useRotasDoUsuario';
import { hrefComParams } from '@/lib/conta';
import { agendarLembretes } from '@/services/notifications';
import type { EstacaoSelecionada } from '@/store/planejamento';
import { useRotasLocais } from '@/store/rotasLocais';
import { useLogado } from '@/store/sessao';
import { colors } from '@/theme/colors';
import type { CriterioRota, LinhaDaEstacao } from '@/types/api';
import { DIAS_DA_SEMANA, ehHorarioValido } from '@/utils/format';
import { linhasDaRota } from '@/utils/rota';

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

function estacaoDosParams(id?: string, nome?: string): EstacaoSelecionada | null {
  return id && nome ? { id: Number(id), nome } : null;
}

export default function RotaSalvaScreen() {
  const params = useLocalSearchParams<Params>();
  const logado = useLogado();
  const { rotas, isPending } = useRotasDoUsuario();
  const existente = params.id ? rotas.find((r) => r.id === Number(params.id)) : undefined;

  if (!logado) {
    return (
      <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
        <Stack.Screen options={{ title: 'Salvar rota' }} />
        <ConviteLogin
          icone={Bookmark}
          titulo="Entre para salvar esta rota"
          mensagem="Suas rotas ficam guardadas na conta e aparecem em qualquer aparelho."
          parametros={{
            motivo: 'salvar-rota',
            proximo: hrefComParams('/rota-salva', params),
          }}
        />
      </SafeAreaView>
    );
  }

  // Editando: espera a lista da conta carregar antes de concluir que a rota não existe.
  if (params.id && !existente) {
    return (
      <SafeAreaView edges={['bottom']} className="flex-1 bg-background p-4">
        {isPending ? (
          <View className="gap-4">
            <Skeleton className="h-24" />
            <Skeleton className="h-14" />
            <Skeleton className="h-24" />
          </View>
        ) : (
          <View className="flex-1 items-center justify-center">
            <Text className="text-center text-ink-muted">Rota não encontrada.</Text>
            <Button rotulo="Voltar" variante="outline" onPress={() => router.back()} className="mt-4" />
          </View>
        )}
      </SafeAreaView>
    );
  }

  return (
    <FormularioRota
      // Remonta se a rota editada mudar, para o estado inicial do formulário valer.
      key={existente?.id ?? 'nova'}
      existente={existente}
      origem={existente?.origem ?? estacaoDosParams(params.origemId, params.origemNome)}
      destino={existente?.destino ?? estacaoDosParams(params.destinoId, params.destinoNome)}
      criterio={existente?.criterio ?? params.criterio ?? 'EQUILIBRADO'}
      agendarInicial={Boolean(existente?.agendamento) || params.agendar === '1'}
    />
  );
}

interface FormularioRotaProps {
  existente: RotaDoUsuario | undefined;
  origem: EstacaoSelecionada | null;
  destino: EstacaoSelecionada | null;
  criterio: CriterioRota;
  agendarInicial: boolean;
}

function FormularioRota({ existente, origem, destino, criterio, agendarInicial }: FormularioRotaProps) {
  const salvarRota = useSalvarRota();
  const atualizarRota = useAtualizarRota();
  const definirLocal = useRotasLocais((s) => s.definir);

  // Para rota nova, as linhas vêm do cálculo que já está em cache (feito no Mapa).
  const { data: rota } = useRota(
    !existente && origem && destino
      ? { origem: origem.nome, destino: destino.nome, criterio }
      : null,
  );
  const linhas: LinhaDaEstacao[] = existente?.linhas.length ? existente.linhas : linhasDaRota(rota);

  const [nome, setNome] = useState(existente?.nome ?? '');
  const [agendar, setAgendar] = useState(agendarInicial);
  const [dias, setDias] = useState<number[]>(existente?.agendamento?.dias ?? DIAS_UTEIS);
  const [horario, setHorario] = useState(existente?.agendamento?.horario ?? '07:00');
  const [salvando, setSalvando] = useState(false);
  const [tentouSalvar, setTentouSalvar] = useState(false);
  const [erroApi, setErroApi] = useState<string | null>(null);

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
    setErroApi(null);
    if (horarioInvalido || semDias) return;

    setSalvando(true);
    const dados = {
      nome: nome.trim() || nomePadrao,
      idEstacaoOrigem: origem.id,
      idEstacaoDestino: destino.id,
      criterio,
    };

    let salva;
    try {
      salva = existente
        ? await atualizarRota.mutateAsync({ id: existente.id, dados })
        : await salvarRota.mutateAsync(dados);
    } catch (erro) {
      setSalvando(false);
      // 401 já abre o login; os demais erros aparecem no formulário.
      if (erro instanceof ApiError && erro.tipo === 'nao-autorizado') return;
      setErroApi(
        erro instanceof ApiError ? erro.message : 'Não foi possível salvar a rota. Tente novamente.',
      );
      return;
    }

    const agendamento = agendar ? { dias: [...dias].sort((a, b) => a - b), horario } : null;
    definirLocal(salva.id, { linhas, agendamento });

    const permitido = await agendarLembretes({
      rotaId: salva.id,
      nome: salva.nome,
      origem: salva.origem.nome,
      destino: salva.destino.nome,
      agendamento,
    }).catch(() => false);
    setSalvando(false);
    void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(() => undefined);

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

          <TextField
            rotulo="Nome da rota"
            dica="Ex.: Casa → Trabalho, Faculdade"
            value={nome}
            onChangeText={setNome}
            placeholder={nomePadrao}
            maxLength={40}
            returnKeyType="done"
          />

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
                <TextField
                  rotulo="Horário"
                  value={horario}
                  onChangeText={(t) => setHorario(mascararHorario(t))}
                  keyboardType="number-pad"
                  placeholder="07:10"
                  maxLength={5}
                  erro={tentouSalvar && horarioInvalido ? 'Use o formato HH:mm, ex.: 07:10.' : null}
                  className="w-32"
                />
              </View>
            )}
          </Card>

          {erroApi && <AlertaErro mensagem={erroApi} />}
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
