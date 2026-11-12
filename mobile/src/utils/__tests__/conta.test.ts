import { describe, expect, it } from '@jest/globals';

import type { LinhaResumo, Rota, TrechoRota } from '@/types/api';

import { formatarData } from '../format';
import { linhasDaRota } from '../rota';
import { ehEmailValido, inicialDoEmail } from '../text';

describe('inicialDoEmail', () => {
  it('usa a primeira letra em maiúscula', () => {
    expect(inicialDoEmail('ana@email.com')).toBe('A');
    expect(inicialDoEmail('  Bruno@x.com ')).toBe('B');
  });

  it('ignora acentos e símbolos iniciais', () => {
    expect(inicialDoEmail('élio@x.com')).toBe('E');
    expect(inicialDoEmail('_.7maria@x.com')).toBe('7');
  });

  it('cai em "?" quando não há e-mail', () => {
    expect(inicialDoEmail(null)).toBe('?');
    expect(inicialDoEmail('')).toBe('?');
  });
});

describe('ehEmailValido', () => {
  it('aceita formatos comuns e recusa os incompletos', () => {
    expect(ehEmailValido('voce@email.com')).toBe(true);
    expect(ehEmailValido(' voce@email.com.br ')).toBe(true);
    expect(ehEmailValido('voce@email')).toBe(false);
    expect(ehEmailValido('voce email@x.com')).toBe(false);
  });
});

describe('formatarData', () => {
  it('escreve a data por extenso', () => {
    expect(formatarData('2026-11-12T09:30:00')).toBe('12 de novembro de 2026');
  });

  it('retorna null para valores inválidos', () => {
    expect(formatarData('ontem')).toBeNull();
    expect(formatarData(null)).toBeNull();
  });
});

describe('linhasDaRota', () => {
  const linha = (id: number, codigo: string): LinhaResumo => ({
    id,
    nome: `Linha ${codigo}`,
    codigo,
    cor: '#000000',
    operadora: 'X',
    tipoTransporte: 'METRO',
    ativa: true,
  });
  const trecho = (l: LinhaResumo) => ({ linha: l }) as TrechoRota;

  it('lista cada linha uma vez, na ordem do trajeto', () => {
    const rota = { trechos: [trecho(linha(3, '3')), trecho(linha(1, '1')), trecho(linha(3, '3'))] };
    expect(linhasDaRota(rota as Rota).map((l) => l.codigo)).toEqual(['3', '1']);
  });

  it('retorna vazio sem rota', () => {
    expect(linhasDaRota(undefined)).toEqual([]);
  });
});
