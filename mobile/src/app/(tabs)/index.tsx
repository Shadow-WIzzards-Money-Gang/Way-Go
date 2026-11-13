import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AvatarConta } from '@/components/AvatarConta';
import { MapaRede } from '@/components/MapaRede';
import { RouteInputCard } from '@/components/RouteInputCard';
import { RotaSugeridaPanel } from '@/components/RotaSugeridaPanel';
import { useRede, useRota } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { abrirLogin, hrefComParams } from '@/lib/conta';
import { type CampoEstacao, usePlanejamento } from '@/store/planejamento';
import { useLogado } from '@/store/sessao';
import { rotaTemLinhaComProblema } from '@/utils/status';

const ALTURA_CARD_ROTA = 120;

export default function MapaScreen() {
  const insets = useSafeAreaInsets();
  const [alturaPainel, setAlturaPainel] = useState(260);

  const { origem, destino, criterio, inverter, definirCriterio } = usePlanejamento();
  const logado = useLogado();
  const { data: rede } = useRede();
  const index = useRedeIndex(rede);

  const params = useMemo(
    () => (origem && destino ? { origem: origem.nome, destino: destino.nome, criterio } : null),
    [origem, destino, criterio],
  );
  const rotaQuery = useRota(params);
  const rotaSugerida = params ? rotaQuery.data : undefined;
  const sugeridaTemProblema = rotaSugerida ? rotaTemLinhaComProblema(rotaSugerida) : false;

  // A escolha "evitar linhas com problema" vale só para o trajeto/critério em que foi feita.
  const chaveTrajeto = params ? `${params.origem}|${params.destino}|${params.criterio}` : '';
  const [escolhaAlternativa, setEscolhaAlternativa] = useState({ chave: '', ativa: false });
  const alternativaAtiva =
    sugeridaTemProblema && escolhaAlternativa.chave === chaveTrajeto && escolhaAlternativa.ativa;
  const alternarRota = (ativa: boolean) => setEscolhaAlternativa({ chave: chaveTrajeto, ativa });

  const alternativaQuery = useRota(
    alternativaAtiva && params ? { ...params, alternativa: true } : null,
  );
  const rotaQueryAtual = alternativaAtiva ? alternativaQuery : rotaQuery;
  const rota = alternativaAtiva ? alternativaQuery.data : rotaSugerida;

  const escolher = (campo: CampoEstacao) =>
    router.push({ pathname: '/selecionar-estacao', params: { campo } });

  /** Salvar exige conta: sem login, passa pelo login e volta direto ao formulário. */
  const abrirRotaSalva = (agendar: boolean) => {
    if (!origem || !destino) return;
    const params = {
      origemId: String(origem.id),
      origemNome: origem.nome,
      destinoId: String(destino.id),
      destinoNome: destino.nome,
      criterio,
      ...(agendar ? { agendar: '1' } : {}),
    };
    if (!logado) {
      abrirLogin({ motivo: 'salvar-rota', proximo: hrefComParams('/rota-salva', params) });
      return;
    }
    router.push({ pathname: '/rota-salva', params });
  };

  return (
    <View className="flex-1 bg-background">
      <MapaRede
        rede={rede}
        index={index}
        rota={rota}
        margens={{ top: insets.top + ALTURA_CARD_ROTA, bottom: alturaPainel }}
      />

      <View className="absolute left-4 right-[72px]" style={{ top: insets.top + 12 }}>
        <RouteInputCard
          origem={origem}
          destino={destino}
          aoEscolher={escolher}
          aoInverter={inverter}
        />
      </View>
      <View className="absolute right-4" style={{ top: insets.top + 12 }}>
        <AvatarConta />
      </View>

      <View
        className="absolute bottom-0 left-0 right-0"
        onLayout={(e) => setAlturaPainel(e.nativeEvent.layout.height)}
        pointerEvents="box-none"
      >
        <RotaSugeridaPanel
          temSelecao={params !== null}
          rota={rota}
          carregando={rotaQueryAtual.isFetching}
          erro={params ? rotaQueryAtual.error : null}
          criterio={criterio}
          aoMudarCriterio={definirCriterio}
          mostrarOpcaoAlternativa={sugeridaTemProblema}
          alternativaAtiva={alternativaAtiva}
          aoAlternar={alternarRota}
          aoTentarNovamente={() => void rotaQueryAtual.refetch()}
          aoAgendar={() => abrirRotaSalva(true)}
          aoSalvar={() => abrirRotaSalva(false)}
        />
      </View>
    </View>
  );
}
