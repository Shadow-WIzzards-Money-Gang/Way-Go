import { Platform, Text, View } from 'react-native';
import MapView, { Marker } from 'react-native-maps';

import { colors } from '@/theme/colors';
import { ESTILO_MAPA } from '@/theme/estiloMapa';

const CENTRO_SAO_PAULO = { latitude: -23.5505, longitude: -46.6333 };

interface SeletorPosicaoProps {
  latitude: number | null;
  longitude: number | null;
  aoEscolher: (latitude: number, longitude: number) => void;
}

/** Mini mapa: toque (ou arraste o pino) para definir a posição da estação. */
export function SeletorPosicao({ latitude, longitude, aoEscolher }: SeletorPosicaoProps) {
  const temPosicao = latitude !== null && longitude !== null;
  const centro = temPosicao ? { latitude, longitude } : CENTRO_SAO_PAULO;

  return (
    <View className="gap-1.5">
      <View className="h-56 overflow-hidden rounded-xl border border-border">
        <MapView
          style={{ flex: 1 }}
          initialRegion={{
            ...centro,
            latitudeDelta: temPosicao ? 0.012 : 0.3,
            longitudeDelta: temPosicao ? 0.012 : 0.3,
          }}
          customMapStyle={ESTILO_MAPA}
          mapType={Platform.OS === 'ios' ? 'mutedStandard' : 'standard'}
          showsPointsOfInterests={false}
          toolbarEnabled={false}
          onPress={(e) =>
            aoEscolher(e.nativeEvent.coordinate.latitude, e.nativeEvent.coordinate.longitude)
          }
        >
          {temPosicao && (
            <Marker
              coordinate={{ latitude, longitude }}
              pinColor={colors.primary.DEFAULT}
              draggable
              onDragEnd={(e) =>
                aoEscolher(e.nativeEvent.coordinate.latitude, e.nativeEvent.coordinate.longitude)
              }
            />
          )}
        </MapView>
      </View>
      <Text className="text-xs text-ink-muted">
        Toque no mapa ou arraste o pino para ajustar a posição.
      </Text>
    </View>
  );
}
