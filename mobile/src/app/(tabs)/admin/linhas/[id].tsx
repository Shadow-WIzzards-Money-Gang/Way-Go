import * as Haptics from 'expo-haptics';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { ArrowDown, ArrowUp, Ban, RotateCcw } from 'lucide-react-native';
import { useState } from 'react';
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Switch,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { LinhaBadge } from '@/components/LinhaBadge';
import { StatusBadge } from '@/components/StatusBadge';
import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Chip } from '@/components/ui/Chip';
import { Skeleton } from '@/components/ui/Skeleton';
import { ErrorState } from '@/components/ui/States';
import { TextField } from '@/components/ui/TextField';
import {
  useAlterarStatusLinha,
  useAtualizarLinha,
  useDesativarLinha,
  useLinha,
  useLinhaStatus,
  useReordenarEstacoes,
} from '@/hooks/queries';
import { colors } from '@/theme/colors';
import type { EstacaoDaLinha, LinhaDetalhe, LinhaStatus, StatusOperacional } from '@/types/api';
import {
  mensagemDeErro,
  mover,
  normalizarCor,
  STATUS_MANUAIS,
  TIPOS_TRANSPORTE,
} from '@/utils/admin';
import { formatarHora } from '@/utils/format';
import { infoStatus } from '@/utils/status';

const sucesso = () =>
  void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(() => undefined);

function TituloSecao({ titulo, descricao }: { titulo: string; descricao?: string }) {
  return (
    <View>
      <Text className="text-base font-semibold text-ink" accessibilityRole="header">
        {titulo}
      </Text>
      {descricao && <Text className="text-sm text-ink-muted">{descricao}</Text>}
    </View>
  );
}

export default function LinhaAdminScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const linhaId = Number(id);
  const { data: linha, isPending, error, refetch } = useLinha(linhaId);
  const status = useLinhaStatus(linhaId);
  const desativar = useDesativarLinha();

  if (isPending) {
    return (
      <View className="gap-4 bg-background p-4">
        <Skeleton className="h-20" />
        <Skeleton className="h-56" />
        <Skeleton className="h-40" />
      </View>
    );
  }
  if (error || !linha) {
    return <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />;
  }

  // Linha nova ainda sem status registrado: o GET responde 404.
  const statusAtual =
    status.error instanceof ApiError && status.error.tipo === 'nao-encontrado'
      ? null
      : (status.data ?? undefined);

  const confirmarDesativacao = () =>
    Alert.alert(
      'Desativar linha?',
      `${linha.nome} deixa de aparecer no mapa e no cálculo de rotas. Você pode reativá-la depois.`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Desativar',
          style: 'destructive',
          onPress: () =>
            desativar.mutate(linha.id, {
              onSuccess: () => {
                sucesso();
                router.back();
              },
              onError: (erro) => {
                const msg = mensagemDeErro(erro, 'Não foi possível desativar a linha.');
                if (msg) Alert.alert('Erro', msg);
              },
            }),
        },
      ],
    );

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <Stack.Screen options={{ title: `Linha ${linha.codigo}` }} />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView contentContainerClassName="gap-4 p-4 pb-8" keyboardShouldPersistTaps="handled">
          <SecaoDados
            // Remonta com os valores do servidor depois de salvar.
            key={`${linha.nome}|${linha.cor}|${linha.operadora}|${linha.ativa}`}
            linha={linha}
          />
          <SecaoStatus
            key={statusAtual ? `${statusAtual.status}|${statusAtual.atualizadoEm}` : 'sem'}
            linhaId={linha.id}
            atual={statusAtual}
          />
          <SecaoOrdem key={linha.estacoes.map((e) => e.id).join(',')} linha={linha} />

          {linha.ativa && (
            <Button
              rotulo="Desativar linha"
              variante="danger"
              icone={Ban}
              carregando={desativar.isPending}
              onPress={confirmarDesativacao}
            />
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function SecaoDados({ linha }: { linha: LinhaDetalhe }) {
  const atualizar = useAtualizarLinha();
  const [nome, setNome] = useState(linha.nome);
  const [cor, setCor] = useState(linha.cor ?? '');
  const [operadora, setOperadora] = useState(linha.operadora);
  const [ativa, setAtiva] = useState(linha.ativa);
  const [erroApi, setErroApi] = useState<string | null>(null);

  const corNormalizada = normalizarCor(cor);
  const erros = {
    nome: nome.trim() ? null : 'Informe o nome.',
    operadora: operadora.trim() ? null : 'Informe a operadora.',
    cor: corNormalizada === null ? 'Use o formato #RRGGBB.' : null,
  };
  const valido = Object.values(erros).every((e) => e === null);
  const alterado =
    nome.trim() !== linha.nome ||
    (corNormalizada ?? cor) !== (linha.cor ?? '') ||
    operadora.trim() !== linha.operadora ||
    ativa !== linha.ativa;
  const tipo = TIPOS_TRANSPORTE.find((t) => t.valor === linha.tipoTransporte)?.rotulo;

  const salvar = () => {
    setErroApi(null);
    atualizar.mutate(
      {
        id: linha.id,
        dados: {
          nome: nome.trim(),
          operadora: operadora.trim(),
          // Vazio não apaga a cor no backend (campo omitido mantém o valor).
          cor: corNormalizada || undefined,
          ativa,
        },
      },
      {
        onSuccess: sucesso,
        onError: (erro) => setErroApi(mensagemDeErro(erro, 'Não foi possível salvar a linha.')),
      },
    );
  };

  return (
    <Card className="gap-4">
      <View className="flex-row items-center gap-3">
        <LinhaBadge codigo={linha.codigo} cor={corNormalizada || linha.cor} />
        <View className="flex-1">
          <Text className="text-base font-semibold text-ink" numberOfLines={1}>
            {nome.trim() || linha.nome}
          </Text>
          <Text className="text-sm text-ink-muted">
            Código {linha.codigo}
            {tipo ? ` · ${tipo}` : ''}
          </Text>
        </View>
      </View>

      <TextField
        rotulo="Nome"
        value={nome}
        onChangeText={setNome}
        maxLength={100}
        erro={erros.nome}
      />
      <View className="flex-row gap-3">
        <TextField
          rotulo="Cor"
          value={cor}
          onChangeText={setCor}
          placeholder="#F28C00"
          autoCapitalize="characters"
          maxLength={7}
          erro={erros.cor}
          className="w-32"
        />
        <TextField
          rotulo="Operadora"
          value={operadora}
          onChangeText={setOperadora}
          maxLength={100}
          erro={erros.operadora}
          className="flex-1"
        />
      </View>
      <View className="flex-row items-center gap-3">
        <View className="flex-1">
          <Text className="text-base font-medium text-ink">Linha ativa</Text>
          <Text className="text-sm text-ink-muted">
            {ativa
              ? 'Aparece no mapa e no cálculo de rotas.'
              : 'Fora do mapa e do cálculo de rotas.'}
          </Text>
        </View>
        <Switch
          value={ativa}
          onValueChange={setAtiva}
          trackColor={{ true: colors.primary.DEFAULT, false: colors.border }}
          accessibilityLabel="Linha ativa"
        />
      </View>

      {erroApi && <AlertaErro mensagem={erroApi} />}
      <Button
        rotulo="Salvar dados"
        onPress={salvar}
        desabilitado={!alterado || !valido}
        carregando={atualizar.isPending}
      />
    </Card>
  );
}

function SecaoStatus({
  linhaId,
  atual,
}: {
  linhaId: number;
  atual: LinhaStatus | null | undefined;
}) {
  const alterar = useAlterarStatusLinha();
  const [status, setStatus] = useState<StatusOperacional>(
    atual && atual.status !== 'DESCONHECIDO' ? atual.status : 'NORMAL',
  );
  const [descricao, setDescricao] = useState(
    atual?.origem === 'ADMINISTRADOR' ? (atual.descricao ?? '') : '',
  );
  const [erroApi, setErroApi] = useState<string | null>(null);

  const aplicar = () => {
    setErroApi(null);
    alterar.mutate(
      { id: linhaId, dados: { status, descricao: descricao.trim() || undefined } },
      {
        onSuccess: sucesso,
        onError: (erro) => setErroApi(mensagemDeErro(erro, 'Não foi possível alterar o status.')),
      },
    );
  };

  const origem =
    atual?.origem === 'ADMINISTRADOR' ? 'definido por administrador' : 'informado pela API';
  const hora = formatarHora(atual?.atualizadoEm);

  return (
    <Card className="gap-3">
      <TituloSecao
        titulo="Status operacional"
        descricao="Vale na hora para todos, até a próxima mudança informada pela API."
      />

      {atual === undefined ? (
        <Skeleton className="h-6 w-40" />
      ) : atual === null ? (
        <Text className="text-sm text-ink-muted">Nenhum status registrado ainda.</Text>
      ) : (
        <View className="flex-row flex-wrap items-center gap-2">
          <Text className="text-sm text-ink-muted">Atual:</Text>
          <StatusBadge status={atual.status} />
          <Text className="text-xs text-ink-muted">
            {origem}
            {hora ? ` às ${hora}` : ''}
          </Text>
        </View>
      )}

      <View className="flex-row flex-wrap gap-2">
        {STATUS_MANUAIS.map((s) => (
          <Chip
            key={s}
            rotulo={infoStatus(s).rotulo}
            selecionado={status === s}
            onPress={() => setStatus(s)}
          />
        ))}
      </View>
      <TextField
        rotulo="Descrição (opcional)"
        dica="Aparece para os passageiros, ex.: motivo e trecho afetado."
        value={descricao}
        onChangeText={setDescricao}
        placeholder="Falha de sinalização entre Sé e Luz"
        maxLength={255}
        multiline
      />
      {erroApi && <AlertaErro mensagem={erroApi} />}
      <Button
        rotulo="Aplicar status"
        variante="outline"
        onPress={aplicar}
        carregando={alterar.isPending}
      />
    </Card>
  );
}

function BotaoMover({
  icone: Icone,
  rotulo,
  desabilitado,
  onPress,
}: {
  icone: typeof ArrowUp;
  rotulo: string;
  desabilitado: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      disabled={desabilitado}
      hitSlop={4}
      accessibilityRole="button"
      accessibilityLabel={rotulo}
      accessibilityState={{ disabled: desabilitado }}
      className={`h-9 w-9 items-center justify-center rounded-lg active:bg-neutral-soft ${
        desabilitado ? 'opacity-30' : ''
      }`}
    >
      <Icone size={18} color={colors.ink.DEFAULT} />
    </Pressable>
  );
}

function SecaoOrdem({ linha }: { linha: LinhaDetalhe }) {
  const reordenar = useReordenarEstacoes();
  const [ordem, setOrdem] = useState<EstacaoDaLinha[]>(linha.estacoes);
  const [erroApi, setErroApi] = useState<string | null>(null);

  const alterada = ordem.some((e, i) => e.id !== linha.estacoes[i]?.id);
  const moverPara = (de: number, para: number) => {
    void Haptics.selectionAsync().catch(() => undefined);
    setOrdem((atual) => mover(atual, de, para));
  };

  const salvar = () => {
    setErroApi(null);
    reordenar.mutate(
      { id: linha.id, idsEstacoes: ordem.map((e) => e.id) },
      {
        onSuccess: sucesso,
        onError: (erro) => setErroApi(mensagemDeErro(erro, 'Não foi possível salvar a ordem.')),
      },
    );
  };

  return (
    <Card className="gap-2">
      <TituloSecao
        titulo="Ordem das estações"
        descricao={
          linha.estacoes.length === 0
            ? 'Esta linha ainda não tem estações.'
            : 'Use as setas para mudar a posição de cada estação no trajeto.'
        }
      />
      {ordem.map((estacao, i) => {
        const mudou = estacao.id !== linha.estacoes[i]?.id;
        return (
          <View
            key={estacao.id}
            className={`flex-row items-center gap-2 border-t border-border py-1.5 ${
              estacao.ativa ? '' : 'opacity-60'
            }`}
          >
            <View
              className={`h-7 w-7 items-center justify-center rounded-full ${
                mudou ? 'bg-primary' : 'bg-neutral-soft'
              }`}
            >
              <Text className={`text-xs font-bold ${mudou ? 'text-white' : 'text-ink-muted'}`}>
                {i + 1}
              </Text>
            </View>
            <Text className="flex-1 text-sm text-ink" numberOfLines={1}>
              {estacao.nome}
            </Text>
            <BotaoMover
              icone={ArrowUp}
              rotulo={`Subir ${estacao.nome}`}
              desabilitado={i === 0}
              onPress={() => moverPara(i, i - 1)}
            />
            <BotaoMover
              icone={ArrowDown}
              rotulo={`Descer ${estacao.nome}`}
              desabilitado={i === ordem.length - 1}
              onPress={() => moverPara(i, i + 1)}
            />
          </View>
        );
      })}
      {erroApi && <AlertaErro mensagem={erroApi} />}
      {alterada && (
        <View className="mt-2 flex-row gap-2">
          <Button
            rotulo="Desfazer"
            variante="ghost"
            icone={RotateCcw}
            onPress={() => setOrdem(linha.estacoes)}
            className="flex-1"
          />
          <Button
            rotulo="Salvar ordem"
            onPress={salvar}
            carregando={reordenar.isPending}
            className="flex-1"
          />
        </View>
      )}
    </Card>
  );
}
