import { router } from 'expo-router';
import { ChevronRight, type LucideIcon, MapPin, TrainFront, UsersRound } from 'lucide-react-native';
import { Pressable, RefreshControl, ScrollView, Text, View } from 'react-native';

import { CabecalhoAba } from '@/components/CabecalhoAba';
import { LinhaBadge } from '@/components/LinhaBadge';
import { StatusBadge } from '@/components/StatusBadge';
import { Card } from '@/components/ui/Card';
import { Screen } from '@/components/ui/Screen';
import { Skeleton } from '@/components/ui/Skeleton';
import { useEstacoes, useRede, useTodasAsLinhas, useUsuarios } from '@/hooks/queries';
import { colors } from '@/theme/colors';
import { pluralizar } from '@/utils/format';
import { ehStatusNormal, infoStatus } from '@/utils/status';
import { compararCodigos } from '@/utils/text';

function Atalho({
  icone: Icone,
  titulo,
  resumo,
  detalhe,
  aoAbrir,
}: {
  icone: LucideIcon;
  titulo: string;
  resumo: string | null;
  detalhe?: string | null;
  aoAbrir: () => void;
}) {
  return (
    <Pressable
      onPress={aoAbrir}
      accessibilityRole="button"
      accessibilityLabel={[titulo, resumo, detalhe].filter(Boolean).join(', ')}
      className="flex-row items-center gap-3 rounded-2xl border border-border bg-surface p-4 active:opacity-80"
    >
      <View className="h-11 w-11 items-center justify-center rounded-xl bg-primary-soft">
        <Icone size={22} color={colors.primary.DEFAULT} />
      </View>
      <View className="flex-1">
        <Text className="text-base font-semibold text-ink">{titulo}</Text>
        {resumo === null ? (
          <Skeleton className="mt-1 h-4 w-24" />
        ) : (
          <Text className="text-sm text-ink-muted">
            {resumo}
            {detalhe ? ` · ${detalhe}` : ''}
          </Text>
        )}
      </View>
      <ChevronRight size={20} color={colors.ink.muted} />
    </Pressable>
  );
}

export default function AdminScreen() {
  const usuarios = useUsuarios(0);
  const linhas = useTodasAsLinhas();
  const rede = useRede();
  const estacoes = useEstacoes({ pagina: 0, tamanho: 1 });

  const inativas = linhas.data?.filter((l) => !l.ativa).length ?? 0;
  const comAlerta = (rede.data?.linhas ?? [])
    .filter((l) => !ehStatusNormal(l.status))
    .sort(
      (a, b) =>
        infoStatus(b.status).severidade - infoStatus(a.status).severidade ||
        compararCodigos(a.codigo, b.codigo),
    );

  const atualizando = usuarios.isRefetching || linhas.isRefetching || rede.isRefetching;
  const atualizar = () => {
    void usuarios.refetch();
    void linhas.refetch();
    void rede.refetch();
    void estacoes.refetch();
  };

  const abrirLinha = (id: number) =>
    router.push({ pathname: '/admin/linhas/[id]', params: { id: String(id) } });

  return (
    <Screen>
      <ScrollView
        contentContainerClassName="gap-3 px-4 pb-8 pt-4"
        refreshControl={
          <RefreshControl
            refreshing={atualizando}
            onRefresh={atualizar}
            tintColor={colors.primary.DEFAULT}
            colors={[colors.primary.DEFAULT]}
          />
        }
      >
        <CabecalhoAba titulo="Administração" subtitulo="Gerencie contas e a rede de transporte" />

        <View className="mt-2 gap-3">
          <Atalho
            icone={UsersRound}
            titulo="Usuários"
            resumo={usuarios.data ? pluralizar(usuarios.data.totalItens, 'conta', 'contas') : null}
            aoAbrir={() => router.push('/admin/usuarios')}
          />
          <Atalho
            icone={TrainFront}
            titulo="Linhas"
            resumo={linhas.data ? pluralizar(linhas.data.length, 'linha', 'linhas') : null}
            detalhe={inativas > 0 ? pluralizar(inativas, 'desativada', 'desativadas') : null}
            aoAbrir={() => router.push('/admin/linhas')}
          />
          <Atalho
            icone={MapPin}
            titulo="Estações"
            resumo={
              estacoes.data ? pluralizar(estacoes.data.totalItens, 'estação', 'estações') : null
            }
            aoAbrir={() => router.push('/admin/estacoes')}
          />
        </View>

        <Text className="mt-4 text-xs font-semibold uppercase tracking-wider text-ink-muted">
          Linhas com alerta
        </Text>
        {rede.isPending ? (
          <Skeleton className="h-16" />
        ) : comAlerta.length === 0 ? (
          <Card>
            <Text className="text-sm text-ink-muted">
              Todas as linhas estão operando normalmente.
            </Text>
          </Card>
        ) : (
          <Card className="py-1">
            {comAlerta.map((linha, i) => (
              <Pressable
                key={linha.id}
                onPress={() => abrirLinha(linha.id)}
                accessibilityRole="button"
                accessibilityLabel={`${linha.nome}, ${infoStatus(linha.status).rotulo}`}
                accessibilityHint="Abre a linha para alterar o status"
                className={`flex-row items-center gap-3 py-3 active:opacity-70 ${
                  i > 0 ? 'border-t border-border' : ''
                }`}
              >
                <LinhaBadge codigo={linha.codigo} cor={linha.cor} tamanho="sm" />
                <Text className="flex-1 text-base text-ink" numberOfLines={1}>
                  {linha.nome}
                </Text>
                <StatusBadge status={linha.status} />
              </Pressable>
            ))}
          </Card>
        )}
      </ScrollView>
    </Screen>
  );
}
