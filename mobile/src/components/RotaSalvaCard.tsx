import { Clock, Pencil, Trash2 } from 'lucide-react-native';
import { memo } from 'react';
import { Pressable, Text, View } from 'react-native';

import type { RotaDoUsuario } from '@/hooks/useRotasDoUsuario';
import { colors } from '@/theme/colors';
import { formatarDias } from '@/utils/format';
import type { StatusRota } from '@/utils/status';

import { LinhaBadge } from './LinhaBadge';
import { Badge } from './ui/Badge';
import { Button } from './ui/Button';

interface RotaSalvaCardProps {
  rota: RotaDoUsuario;
  status: StatusRota;
  excluindo?: boolean;
  aoAbrir: (rota: RotaDoUsuario) => void;
  aoEditar: (rota: RotaDoUsuario) => void;
  aoAgendar: (rota: RotaDoUsuario) => void;
  aoExcluir: (rota: RotaDoUsuario) => void;
}

export const RotaSalvaCard = memo(function RotaSalvaCard({
  rota,
  status,
  excluindo = false,
  aoAbrir,
  aoEditar,
  aoAgendar,
  aoExcluir,
}: RotaSalvaCardProps) {
  const agendamento = rota.agendamento
    ? `${formatarDias(rota.agendamento.dias)} · ${rota.agendamento.horario}`
    : 'Sem agendamento';

  return (
    <View
      className={`mb-3 rounded-2xl border border-border bg-surface ${excluindo ? 'opacity-60' : ''}`}
    >
      <Pressable
        onPress={() => aoAbrir(rota)}
        disabled={excluindo}
        accessibilityRole="button"
        accessibilityLabel={`${rota.nome}, ${rota.origem.nome} para ${rota.destino.nome}, ${status.rotulo}`}
        accessibilityHint="Mostra a rota no mapa"
        className="p-4 pb-3 active:opacity-80"
      >
        <View className="flex-row items-start justify-between gap-2">
          <Text className="flex-1 text-lg font-semibold text-ink" numberOfLines={1}>
            {rota.nome}
          </Text>
          <Badge rotulo={status.rotulo} tom={status.tom} />
        </View>
        <Text className="text-sm text-ink-muted" numberOfLines={1}>
          {rota.origem.nome} → {rota.destino.nome}
        </Text>
        <View className="mt-2.5 flex-row flex-wrap items-center gap-2">
          {rota.linhas.map((l) => (
            <LinhaBadge key={l.id} codigo={l.codigo} cor={l.cor} tamanho="sm" />
          ))}
          <View className="flex-row items-center gap-1">
            <Clock size={14} color={colors.ink.muted} />
            <Text className="text-sm text-ink-muted">{agendamento}</Text>
          </View>
        </View>
      </Pressable>
      <View className="mx-4 h-px bg-border" />
      <View className="flex-row gap-1 px-2 py-1.5">
        <Button
          rotulo="Editar"
          variante="ghost"
          icone={Pencil}
          compacto
          desabilitado={excluindo}
          onPress={() => aoEditar(rota)}
        />
        <Button
          rotulo="Agendar"
          variante="ghost"
          icone={Clock}
          compacto
          desabilitado={excluindo}
          onPress={() => aoAgendar(rota)}
        />
        <Button
          rotulo={excluindo ? 'Excluindo' : 'Excluir'}
          variante="danger"
          icone={Trash2}
          compacto
          carregando={excluindo}
          onPress={() => aoExcluir(rota)}
        />
      </View>
    </View>
  );
});
