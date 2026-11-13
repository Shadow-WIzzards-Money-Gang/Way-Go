import { Check, ChevronRight, CircleDot, MapPin, Shuffle, X } from 'lucide-react-native';
import { Pressable, Text, View } from 'react-native';

import type { CampoEstacao } from '@/store/planejamento';
import { colors } from '@/theme/colors';
import type { EstacaoRede, LinhaRede } from '@/types/api';
import { ehStatusNormal, infoStatus } from '@/utils/status';
import { formatarNomeLinha } from '@/utils/text';

import { AvisoBanner } from './AvisoBanner';
import { LinhaBadge } from './LinhaBadge';
import { Button } from './ui/Button';

interface EstacaoMapaCardProps {
  estacao: EstacaoRede;
  /** Linhas da estação, já ordenadas pelo código. */
  linhas: LinhaRede[];
  /** Papel atual da estação no planejamento (já é a origem ou o destino?). */
  papel: CampoEstacao | null;
  aoDefinir: (campo: CampoEstacao) => void;
  aoVerDetalhes: () => void;
  /** Toque numa das linhas da estação: abre os detalhes da linha. */
  aoAbrirLinha: (linhaId: number) => void;
  aoFechar: () => void;
}

function subtitulo(linhas: LinhaRede[]): string {
  if (linhas.length === 0) return 'Sem linhas em operação';
  if (linhas.length === 1) return formatarNomeLinha(linhas[0].nome);
  return `Integração entre ${linhas.length} linhas`;
}

/** Card da estação tocada no mapa: linhas, status e os atalhos "Sair daqui" / "Ir para cá". */
export function EstacaoMapaCard({
  estacao,
  linhas,
  papel,
  aoDefinir,
  aoVerDetalhes,
  aoAbrirLinha,
  aoFechar,
}: EstacaoMapaCardProps) {
  const comProblema = linhas.filter((l) => !ehStatusNormal(l.status));
  const integracao = linhas.length > 1;

  return (
    <View className="gap-4">
      <View className="flex-row items-start gap-3">
        <View className="flex-1 gap-1">
          <Text className="text-xs font-semibold uppercase tracking-wider text-ink-muted">
            Estação
          </Text>
          <Text
            className="text-2xl font-bold text-ink"
            numberOfLines={2}
            accessibilityRole="header"
          >
            {estacao.nome}
          </Text>
          <View className="flex-row items-center gap-1.5">
            {integracao && <Shuffle size={14} color={colors.ink.muted} />}
            <Text className="text-sm text-ink-muted" numberOfLines={1}>
              {subtitulo(linhas)}
            </Text>
          </View>
        </View>
        <Pressable
          onPress={aoFechar}
          accessibilityRole="button"
          accessibilityLabel="Fechar estação"
          hitSlop={8}
          className="h-11 w-11 items-center justify-center rounded-full bg-neutral-soft active:opacity-70"
        >
          <X size={20} color={colors.ink.DEFAULT} />
        </Pressable>
      </View>

      {linhas.length > 0 && (
        <View className="flex-row flex-wrap gap-2">
          {linhas.map((l) => (
            <Pressable
              key={l.id}
              onPress={() => aoAbrirLinha(l.id)}
              accessibilityRole="button"
              accessibilityLabel={`${formatarNomeLinha(l.nome)}, ${infoStatus(l.status).rotulo}`}
              accessibilityHint="Abre as estações e o status da linha"
              hitSlop={4}
              className="min-h-[36px] flex-row items-center gap-1.5 rounded-full border border-border bg-background pl-1 pr-2 active:opacity-70"
            >
              <LinhaBadge codigo={l.codigo} cor={l.cor} tamanho="sm" />
              <View
                className="h-2 w-2 rounded-full"
                style={{
                  backgroundColor: ehStatusNormal(l.status)
                    ? colors.success.DEFAULT
                    : infoStatus(l.status).tom === 'danger'
                      ? colors.danger.DEFAULT
                      : colors.warning.DEFAULT,
                }}
              />
              <Text className="text-xs font-medium text-ink-muted">
                {infoStatus(l.status).rotulo}
              </Text>
              <ChevronRight size={14} color={colors.ink.subtle} />
            </Pressable>
          ))}
        </View>
      )}

      {comProblema.map((l) => (
        <AvisoBanner
          key={l.id}
          titulo={formatarNomeLinha(l.nome)}
          mensagem={l.statusDescricao ?? infoStatus(l.status).rotulo}
        />
      ))}

      <View className="flex-row gap-3">
        <Button
          rotulo={papel === 'origem' ? 'É a origem' : 'Sair daqui'}
          icone={papel === 'origem' ? Check : CircleDot}
          variante="outline"
          desabilitado={papel === 'origem'}
          onPress={() => aoDefinir('origem')}
          accessibilityHint="Define esta estação como origem da rota"
          className="flex-1"
        />
        <Button
          rotulo={papel === 'destino' ? 'É o destino' : 'Ir para cá'}
          icone={papel === 'destino' ? Check : MapPin}
          desabilitado={papel === 'destino'}
          onPress={() => aoDefinir('destino')}
          accessibilityHint="Define esta estação como destino da rota"
          className="flex-1"
        />
      </View>

      <Pressable
        onPress={aoVerDetalhes}
        accessibilityRole="button"
        accessibilityLabel={`Ver detalhes da estação ${estacao.nome}`}
        className="min-h-[44px] flex-row items-center justify-center gap-1 active:opacity-70"
      >
        <Text className="text-sm font-semibold text-primary">Ver detalhes da estação</Text>
        <ChevronRight size={16} color={colors.primary.DEFAULT} />
      </Pressable>
    </View>
  );
}
