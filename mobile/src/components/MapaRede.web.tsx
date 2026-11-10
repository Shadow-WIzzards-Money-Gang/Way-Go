import { Map } from 'lucide-react-native';
import { View } from 'react-native';

import { EmptyState } from './ui/States';

/** react-native-maps não roda na web; mostra um aviso no lugar do mapa. */
export function MapaRede(_props: unknown) {
  return (
    <View className="flex-1 items-center justify-center bg-neutral-soft">
      <EmptyState
        icone={Map}
        titulo="Mapa disponível no app"
        mensagem="Abra o WayGo no Android ou iOS para ver o mapa da rede."
      />
    </View>
  );
}
