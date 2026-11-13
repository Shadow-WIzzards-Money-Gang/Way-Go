import { describe, expect, it } from '@jest/globals';

import { ApiError } from '@/api/errors';

import { formatarCoordenada, lerCoordenada, mensagemDeErro, mover, normalizarCor } from '../admin';

describe('normalizarCor', () => {
  it('aceita com ou sem # e padroniza em maiúsculas', () => {
    expect(normalizarCor('#0455a1')).toBe('#0455A1');
    expect(normalizarCor(' 0455A1 ')).toBe('#0455A1');
  });

  it('vazio continua vazio e formatos inválidos viram null', () => {
    expect(normalizarCor('')).toBe('');
    expect(normalizarCor('azul')).toBeNull();
    expect(normalizarCor('#123')).toBeNull();
  });
});

describe('lerCoordenada', () => {
  it('aceita ponto ou vírgula decimal', () => {
    expect(lerCoordenada('-23.5505', 90)).toBe(-23.5505);
    expect(lerCoordenada('-46,6333', 180)).toBe(-46.6333);
  });

  it('rejeita texto inválido e valores fora do limite', () => {
    expect(lerCoordenada('', 90)).toBeNull();
    expect(lerCoordenada('abc', 90)).toBeNull();
    expect(lerCoordenada('91', 90)).toBeNull();
    expect(lerCoordenada('-180', 180)).toBe(-180);
  });
});

describe('formatarCoordenada', () => {
  it('limita a 7 casas e trata ausência', () => {
    expect(formatarCoordenada(-23.123456789)).toBe('-23.1234568');
    expect(formatarCoordenada(null)).toBe('');
  });
});

describe('mover', () => {
  it('move um item sem alterar a lista original', () => {
    const original = ['a', 'b', 'c'];
    expect(mover(original, 0, 2)).toEqual(['b', 'c', 'a']);
    expect(mover(original, 2, 1)).toEqual(['a', 'c', 'b']);
    expect(original).toEqual(['a', 'b', 'c']);
  });

  it('ignora destinos fora da lista', () => {
    expect(mover(['a', 'b'], 0, -1)).toEqual(['a', 'b']);
    expect(mover(['a', 'b'], 1, 2)).toEqual(['a', 'b']);
  });
});

describe('mensagemDeErro', () => {
  it('usa a mensagem da API, menos no 401', () => {
    expect(mensagemDeErro(new ApiError('Código repetido', 'conflito', 409), 'x')).toBe(
      'Código repetido',
    );
    expect(mensagemDeErro(new ApiError('Entre', 'nao-autorizado', 401), 'x')).toBeNull();
    expect(mensagemDeErro(new Error('boom'), 'Padrão')).toBe('Padrão');
  });
});
