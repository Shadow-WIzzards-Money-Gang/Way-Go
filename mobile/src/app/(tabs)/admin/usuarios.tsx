import { UsersRound } from 'lucide-react-native';
import { useRef, useState } from 'react';
import { FlatList, RefreshControl, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { Paginacao } from '@/components/ui/Paginacao';
import { SkeletonLista } from '@/components/ui/Skeleton';
import { EmptyState, ErrorState } from '@/components/ui/States';
import { useEmailDaSessao } from '@/store/sessao';
import { useUsuarios } from '@/hooks/queries';
import { colors } from '@/theme/colors';
import type { Role, Usuario } from '@/types/api';
import { formatarData, pluralizar } from '@/utils/format';
import type { Tom } from '@/utils/status';

const PERFIS: Record<Role, { rotulo: string; tom: Tom }> = {
  ADMINISTRADOR: { rotulo: 'Administrador', tom: 'danger' },
  MODERADOR: { rotulo: 'Moderador', tom: 'warning' },
  USUARIO: { rotulo: 'Passageiro', tom: 'neutral' },
};

function ItemUsuario({ usuario, ehVoce }: { usuario: Usuario; ehVoce: boolean }) {
  const desde = formatarData(usuario.criadoEm);
  return (
    <View
      className="mb-2 flex-row items-center gap-3 rounded-2xl border border-border bg-surface p-3"
      accessible
      accessibilityLabel={`${usuario.email}${ehVoce ? ' (você)' : ''}, ${usuario.roles
        .map((r) => PERFIS[r]?.rotulo ?? r)
        .join(', ')}${desde ? `, desde ${desde}` : ''}`}
    >
      <Avatar email={usuario.email} />
      <View className="flex-1 gap-1">
        <Text className="text-base font-medium text-ink" numberOfLines={1}>
          {usuario.email}
          {ehVoce && <Text className="text-ink-muted"> (você)</Text>}
        </Text>
        <View className="flex-row flex-wrap items-center gap-1.5">
          {usuario.roles.map((r) => (
            <Badge key={r} rotulo={PERFIS[r]?.rotulo ?? r} tom={PERFIS[r]?.tom ?? 'neutral'} />
          ))}
          {desde && <Text className="text-xs text-ink-muted">desde {desde}</Text>}
        </View>
      </View>
    </View>
  );
}

export default function UsuariosAdminScreen() {
  const [pagina, setPagina] = useState(0);
  const emailDaSessao = useEmailDaSessao();
  const listaRef = useRef<FlatList<Usuario>>(null);
  const { data, isPending, isPlaceholderData, error, refetch, isRefetching } = useUsuarios(pagina);

  const mudarPagina = (nova: number) => {
    setPagina(nova);
    listaRef.current?.scrollToOffset({ offset: 0, animated: false });
  };

  if (error && !data) {
    return <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />;
  }

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <FlatList
        ref={listaRef}
        data={data?.conteudo ?? []}
        keyExtractor={(u) => u.id}
        className={isPlaceholderData ? 'opacity-60' : undefined}
        contentContainerClassName="px-4 pb-6 pt-3"
        ListHeaderComponent={
          data ? (
            <Text className="mb-2 text-xs text-ink-muted" accessibilityLiveRegion="polite">
              {pluralizar(data.totalItens, 'conta cadastrada', 'contas cadastradas')}, das mais
              recentes para as mais antigas
            </Text>
          ) : undefined
        }
        ListEmptyComponent={
          isPending ? (
            <SkeletonLista itens={8} />
          ) : (
            <EmptyState icone={UsersRound} titulo="Nenhuma conta cadastrada" />
          )
        }
        renderItem={({ item }) => (
          <ItemUsuario usuario={item} ehVoce={item.email === emailDaSessao} />
        )}
        refreshControl={
          <RefreshControl
            refreshing={isRefetching && !isPlaceholderData}
            onRefresh={() => void refetch()}
            tintColor={colors.primary.DEFAULT}
            colors={[colors.primary.DEFAULT]}
          />
        }
      />
      {data && data.totalPaginas > 1 && (
        <Paginacao
          pagina={pagina}
          totalPaginas={data.totalPaginas}
          aoMudar={mudarPagina}
          carregando={isPlaceholderData}
        />
      )}
    </SafeAreaView>
  );
}
