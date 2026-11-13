import { Text } from 'react-native';

/** react-native-maps não roda na web: a posição é informada só pelos campos de latitude/longitude. */
export function SeletorPosicao(_props: {
  latitude: number | null;
  longitude: number | null;
  aoEscolher: (latitude: number, longitude: number) => void;
}) {
  return (
    <Text className="text-xs text-ink-muted">
      No app para Android e iOS dá para escolher a posição tocando no mapa.
    </Text>
  );
}
