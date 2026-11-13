import type { MapStyleElement } from 'react-native-maps';

import { colors } from './colors';

/**
 * Estilo claro e discreto do Google Maps (Android), como no protótipo docs/telas/tela-mapa.png:
 * some com comércios, ícones e o transporte público do Google (que competiria com as nossas
 * linhas), e deixa ruas e nomes em tons neutros para as linhas e estações serem o destaque.
 * No iOS (Apple Maps) o equivalente é mapType="mutedStandard".
 */
export const ESTILO_MAPA: MapStyleElement[] = [
  { elementType: 'geometry', stylers: [{ color: colors.background }] },
  { elementType: 'labels.icon', stylers: [{ visibility: 'off' }] },
  { elementType: 'labels.text.fill', stylers: [{ color: colors.ink.subtle }] },
  { elementType: 'labels.text.stroke', stylers: [{ color: colors.surface }, { weight: 3 }] },

  { featureType: 'poi', stylers: [{ visibility: 'off' }] },
  {
    featureType: 'poi.park',
    elementType: 'geometry',
    stylers: [{ visibility: 'on' }, { color: '#E3F1E7' }],
  },
  { featureType: 'transit', stylers: [{ visibility: 'off' }] },

  { featureType: 'administrative', elementType: 'geometry', stylers: [{ visibility: 'off' }] },
  {
    featureType: 'administrative.locality',
    elementType: 'labels.text.fill',
    stylers: [{ color: colors.ink.muted }],
  },
  { featureType: 'administrative.neighborhood', stylers: [{ visibility: 'off' }] },

  { featureType: 'landscape.man_made', elementType: 'geometry', stylers: [{ color: '#EEF1F6' }] },

  { featureType: 'road', elementType: 'geometry', stylers: [{ color: colors.surface }] },
  { featureType: 'road', elementType: 'geometry.stroke', stylers: [{ color: colors.border }] },
  { featureType: 'road.highway', elementType: 'geometry', stylers: [{ color: '#E9EDF3' }] },
  { featureType: 'road.highway', elementType: 'labels', stylers: [{ visibility: 'off' }] },
  { featureType: 'road.local', elementType: 'labels', stylers: [{ visibility: 'off' }] },

  { featureType: 'water', elementType: 'geometry', stylers: [{ color: '#D6E4F5' }] },
  { featureType: 'water', elementType: 'labels', stylers: [{ visibility: 'off' }] },
];
