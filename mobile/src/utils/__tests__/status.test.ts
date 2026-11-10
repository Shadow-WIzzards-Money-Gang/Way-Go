import { describe, expect, it } from '@jest/globals';
import { corDeTextoSobre, corSegura } from '../color';
import { infoStatus, statusDaRota } from '../status';
import { contemTexto, formatarNomeLinha, normalizar } from '../text';

describe('statusDaRota', () => {
  it('usa o pior status entre as linhas da rota', () => {
    expect(statusDaRota(['NORMAL', 'NORMAL']).rotulo).toBe('Normal');
    expect(statusDaRota(['NORMAL', 'VELOCIDADE_REDUZIDA']).rotulo).toBe('Atenção');
    expect(statusDaRota(['VELOCIDADE_REDUZIDA', 'PARALISADA']).rotulo).toBe('Interrompida');
  });

  it('sem informação quando não há status conhecido', () => {
    expect(statusDaRota([]).rotulo).toBe('Sem informação');
    expect(statusDaRota(['DESCONHECIDO']).rotulo).toBe('Sem informação');
  });
});

describe('infoStatus', () => {
  it('mapeia status do backend para rótulo e tom', () => {
    expect(infoStatus('PARALISADA')).toMatchObject({ rotulo: 'Paralisada', tom: 'danger' });
    expect(infoStatus('VELOCIDADE_REDUZIDA').tom).toBe('warning');
  });
});

describe('texto', () => {
  it('busca ignorando acentos e maiúsculas', () => {
    expect(normalizar('Brás')).toBe('bras');
    expect(contemTexto('José Bonifácio', 'jose bon')).toBe(true);
    expect(contemTexto('Luz', '')).toBe(true);
  });

  it('usa traço tipográfico no nome da linha', () => {
    expect(formatarNomeLinha('Linha 11 - Coral')).toBe('Linha 11 – Coral');
  });
});

describe('corDeTextoSobre', () => {
  it('usa texto escuro em cores claras e claro em cores escuras', () => {
    expect(corDeTextoSobre('#FFD700')).toBe('#0F172A');
    expect(corDeTextoSobre('#1C146B')).toBe('#FFFFFF');
  });
});

describe('corSegura', () => {
  it('converte hex em rgba e aplica opacidade', () => {
    expect(corSegura('#FF7F50')).toBe('rgba(255, 127, 80, 1)');
    expect(corSegura('#1266af', 0.33)).toBe('rgba(18, 102, 175, 0.33)');
  });

  it('nunca devolve cor inválida para o mapa', () => {
    expect(corSegura(null)).toBe('rgba(100, 116, 139, 1)');
    expect(corSegura('azul')).toBe('rgba(100, 116, 139, 1)');
  });
});
