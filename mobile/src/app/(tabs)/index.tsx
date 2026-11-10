import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { MapaRede } from '@/components/MapaRede';
import { RouteInputCard } from '@/components/RouteInputCard';
import { RotaSugeridaPanel } from '@/components/RotaSugeridaPanel';
import { useRede, useRota } from '@/hooks/queries';
import { useRedeIndex } from '@/hooks/useRedeIndex';
import { type CampoEstacao, usePlanejamento } from '@/store/planejamento';

const ALTURA_CARD_ROTA = 120;

export default function MapaScreen() {
  const insets = useSafeAreaInsets();
  const [alturaPainel, setAlturaPainel] = useState(260);

  const { origem, destino, criterio, inverter, definirCriterio } = usePlanejamento();
  const { data: rede } = useRede();
  const index = useRedeIndex(rede);

  const params = useMemo(
    () => (origem && destino ? { origem: origem.nome, destino: destino.nome, criterio } : null),
    [origem, destino, criterio],
  );
  const rotaQuery = useRota(params);
  const rota = params ? rotaQuery.data : undefined;

  const escolher = (campo: CampoEstacao) =>
    router.push({ pathname: '/selecionar-estacao', params: { campo } });

  const abrirRotaSalva = (agendar: boolean) => {
    if (!origem || !destino) return;
    router.push({
      pathname: '/rota-salva',
      params: {
        origemId: String(origem.id),
        origemNome: origem.nome,
        destinoId: String(destino.id),
        destinoNome: destino.nome,
        criterio,
        ...(agendar ? { agendar: '1' } : {}),
      },
    });
  };

  return (
    <View className="flex-1 bg-background">
      <MapaRede
        rede={rede}
        index={index}
        rota={rota}
        margens={{ top: insets.top + ALTURA_CARD_ROTA, bottom: alturaPainel }}
      />

      <View className="absolute left-4 right-4" style={{ top: insets.top + 12 }}>
        <RouteInputCard
          origem={origem}
          destino={destino}
          aoEscolher={escolher}
          aoInverter={inverter}
        />
      </View>

      <View
        className="absolute bottom-0 left-0 right-0"
        onLayout={(e) => setAlturaPainel(e.nativeEvent.layout.height)}
        pointerEvents="box-none"
      >
        <RotaSugeridaPanel
          temSelecao={params !== null}
          rota={rota}
          carregando={rotaQuery.isFetching}
          erro={params ? rotaQuery.error : null}
          criterio={criterio}
          aoMudarCriterio={definirCriterio}
          aoTentarNovamente={() => void rotaQuery.refetch()}
          aoAgendar={() => abrirRotaSalva(true)}
          aoSalvar={() => abrirRotaSalva(false)}
        />
      </View>
    </View>
  );
}
