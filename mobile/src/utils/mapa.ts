/** Regras puras do mapa da rede: zoom, visibilidade das estações e enquadramento. */

export interface Coordenada {
  latitude: number;
  longitude: number;
}

export interface Regiao extends Coordenada {
  latitudeDelta: number;
  longitudeDelta: number;
}

/**
 * Faixas de zoom do mapa (pela largura visível em graus de longitude):
 * - cidade: a rede inteira; só integrações aparecem, para não virar um amontoado de pontos.
 * - bairro: todas as estações, sem nomes.
 * - rua: todas as estações, com o nome das que estão na tela.
 */
export type NivelZoom = 'cidade' | 'bairro' | 'rua';

export const LIMITE_ZOOM_CIDADE = 0.22;
export const LIMITE_ZOOM_RUA = 0.045;

export function nivelDeZoom(longitudeDelta: number): NivelZoom {
  if (longitudeDelta > LIMITE_ZOOM_CIDADE) return 'cidade';
  if (longitudeDelta > LIMITE_ZOOM_RUA) return 'bairro';
  return 'rua';
}

export function paraCoordenada(e: {
  latitude: number | null;
  longitude: number | null;
}): Coordenada | null {
  if (e.latitude == null || e.longitude == null) return null;
  const latitude = Number(e.latitude);
  const longitude = Number(e.longitude);
  return Number.isFinite(latitude) && Number.isFinite(longitude) ? { latitude, longitude } : null;
}

export function coordenadasDe(
  estacoes: { latitude: number | null; longitude: number | null }[],
): Coordenada[] {
  return estacoes.map(paraCoordenada).filter((c): c is Coordenada => c !== null);
}

/** A coordenada está na região visível (com uma folga proporcional, para não "piscar" na borda)? */
export function dentroDaRegiao(coord: Coordenada, regiao: Regiao, folga = 0.15): boolean {
  const meiaLat = (regiao.latitudeDelta / 2) * (1 + folga);
  const meiaLng = (regiao.longitudeDelta / 2) * (1 + folga);
  return (
    Math.abs(coord.latitude - regiao.latitude) <= meiaLat &&
    Math.abs(coord.longitude - regiao.longitude) <= meiaLng
  );
}

/**
 * Deve desenhar o ponto da estação? Destacadas (selecionada, origem, destino, parte da rota)
 * aparecem sempre; integrações aparecem em qualquer zoom; as demais só de perto.
 */
export function mostrarEstacao(nivel: NivelZoom, ehIntegracao: boolean, destacada: boolean) {
  return destacada || ehIntegracao || nivel !== 'cidade';
}

/** Região que aproxima numa estação sem afastar quem já está perto. */
export function regiaoFocadaEm(coord: Coordenada, atual: Regiao, deltaMaximo = 0.04): Regiao {
  const proporcao = atual.latitudeDelta / atual.longitudeDelta || 1;
  const longitudeDelta = Math.min(atual.longitudeDelta, deltaMaximo);
  return { ...coord, longitudeDelta, latitudeDelta: longitudeDelta * proporcao };
}
