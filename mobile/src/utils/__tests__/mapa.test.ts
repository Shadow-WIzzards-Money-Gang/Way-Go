import { describe, expect, it } from '@jest/globals';

import {
  coordenadasDe,
  dentroDaRegiao,
  mostrarEstacao,
  nivelDeZoom,
  paraCoordenada,
  regiaoFocadaEm,
} from '../mapa';

const SE = { latitude: -23.55, longitude: -46.633 };
const REGIAO = { ...SE, latitudeDelta: 0.04, longitudeDelta: 0.02 };

describe('nivelDeZoom', () => {
  it('separa cidade, bairro e rua pela largura visível', () => {
    expect(nivelDeZoom(0.45)).toBe('cidade');
    expect(nivelDeZoom(0.1)).toBe('bairro');
    expect(nivelDeZoom(0.02)).toBe('rua');
  });
});

describe('mostrarEstacao', () => {
  it('de longe mostra só integrações e estações destacadas', () => {
    expect(mostrarEstacao('cidade', false, false)).toBe(false);
    expect(mostrarEstacao('cidade', true, false)).toBe(true);
    expect(mostrarEstacao('cidade', false, true)).toBe(true);
  });

  it('de perto mostra todas', () => {
    expect(mostrarEstacao('bairro', false, false)).toBe(true);
    expect(mostrarEstacao('rua', false, false)).toBe(true);
  });
});

describe('coordenadas', () => {
  it('ignora estações sem latitude/longitude', () => {
    expect(paraCoordenada({ latitude: null, longitude: -46 })).toBeNull();
    expect(
      coordenadasDe([
        { latitude: -23, longitude: -46 },
        { latitude: null, longitude: null },
      ]),
    ).toEqual([{ latitude: -23, longitude: -46 }]);
  });
});

describe('dentroDaRegiao', () => {
  it('considera a área visível com folga', () => {
    expect(dentroDaRegiao(SE, REGIAO)).toBe(true);
    expect(dentroDaRegiao({ latitude: -23.55, longitude: -46.6435 }, REGIAO)).toBe(true);
    expect(dentroDaRegiao({ latitude: -23.7, longitude: -46.633 }, REGIAO)).toBe(false);
  });
});

describe('regiaoFocadaEm', () => {
  it('aproxima quem está longe e mantém o zoom de quem já está perto', () => {
    const longe = { ...SE, latitudeDelta: 0.45, longitudeDelta: 0.45 };
    expect(regiaoFocadaEm(SE, longe).longitudeDelta).toBe(0.04);
    expect(regiaoFocadaEm(SE, REGIAO).longitudeDelta).toBe(0.02);
    expect(regiaoFocadaEm(SE, REGIAO).latitudeDelta).toBeCloseTo(0.04);
  });
});
