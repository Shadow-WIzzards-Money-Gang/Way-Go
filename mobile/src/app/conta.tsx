import { router } from 'expo-router';
import { CalendarDays, KeyRound, LogOut, Mail, ShieldCheck, UserRound } from 'lucide-react-native';
import { Alert, ScrollView, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '@/api/errors';
import { ConviteLogin } from '@/components/ConviteLogin';
import { AvisoBanner } from '@/components/AvisoBanner';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/Skeleton';
import { ErrorState } from '@/components/ui/States';
import { useUsuarioAtual } from '@/hooks/queries';
import { encerrarSessao } from '@/lib/conta';
import { useEmailDaSessao } from '@/store/sessao';
import { colors } from '@/theme/colors';
import type { Role } from '@/types/api';
import { formatarData } from '@/utils/format';

const NOME_PERFIL: Record<Role, string> = {
  USUARIO: 'Passageiro',
  MODERADOR: 'Moderador',
  ADMINISTRADOR: 'Administrador',
};

function LinhaInfo({
  icone: Icone,
  rotulo,
  valor,
  ultima = false,
}: {
  icone: typeof Mail;
  rotulo: string;
  valor: string;
  ultima?: boolean;
}) {
  return (
    <View
      className={`flex-row items-center gap-3 py-3 ${ultima ? '' : 'border-b border-border'}`}
      accessible
      accessibilityLabel={`${rotulo}: ${valor}`}
    >
      <Icone size={20} color={colors.ink.muted} />
      <View className="flex-1">
        <Text className="text-xs text-ink-muted">{rotulo}</Text>
        <Text className="text-base font-medium text-ink" numberOfLines={1}>
          {valor}
        </Text>
      </View>
    </View>
  );
}

export default function ContaScreen() {
  const emailDaSessao = useEmailDaSessao();
  const { data: usuario, isPending, error, refetch } = useUsuarioAtual();

  const sair = () =>
    Alert.alert('Encerrar sessão?', 'Suas rotas continuam salvas na conta.', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Encerrar sessão',
        style: 'destructive',
        onPress: () => {
          router.back();
          void encerrarSessao();
        },
      },
    ]);

  if (!emailDaSessao) {
    return (
      <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
        <ConviteLogin
          icone={UserRound}
          titulo="Você não está conectado"
          mensagem="Entre para ver os dados da sua conta."
        />
      </SafeAreaView>
    );
  }

  // Se /usuarios/me ainda não responder, mostramos o que a sessão já sabe.
  const semDetalhes =
    error instanceof ApiError && (error.tipo === 'nao-encontrado' || error.tipo === 'servidor');
  const email = usuario?.email ?? emailDaSessao;
  const perfis = usuario?.roles.map((r) => NOME_PERFIL[r] ?? r).join(', ');
  const membroDesde = formatarData(usuario?.criadoEm);

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <ScrollView contentContainerClassName="gap-4 p-4">
        <View className="items-center gap-3 py-4">
          <Avatar email={email} tamanho="lg" />
          <Text className="text-lg font-semibold text-ink" numberOfLines={1}>
            {email}
          </Text>
        </View>

        {isPending ? (
          <Skeleton className="h-44" />
        ) : error && !semDetalhes ? (
          <ErrorState erro={error} aoTentarNovamente={() => void refetch()} />
        ) : (
          <>
            {semDetalhes && (
              <AvisoBanner mensagem="Detalhes da conta indisponíveis no momento. Tente mais tarde." />
            )}
            <Card className="py-1">
              <LinhaInfo icone={Mail} rotulo="E-mail" valor={email} ultima={!usuario} />
              {perfis && <LinhaInfo icone={ShieldCheck} rotulo="Perfil" valor={perfis} />}
              {membroDesde && (
                <LinhaInfo icone={CalendarDays} rotulo="Membro desde" valor={membroDesde} ultima />
              )}
            </Card>
          </>
        )}

        <Button
          rotulo="Alterar senha"
          variante="outline"
          icone={KeyRound}
          onPress={() => router.push('/alterar-senha')}
        />
        <Button rotulo="Encerrar sessão" variante="danger" icone={LogOut} onPress={sair} />
      </ScrollView>
    </SafeAreaView>
  );
}
