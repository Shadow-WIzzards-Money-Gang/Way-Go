import { describe, expect, it } from '@jest/globals';

import { chaveDoTrajeto, rotaDaNotificacao } from '../notificacao';

const DADOS = {
  tipo: 'STATUS_LINHA',
  idRotaSalva: '7',
  origemId: '1',
  destinoId: '2',
  origemNome: 'Sé',
  destinoNome: 'Luz',
  criterio: 'MENOR_TEMPO',
  alternativa: 'true',
};

describe('rotaDaNotificacao', () => {
  it('converte os dados do push na rota para o mapa', () => {
    expect(rotaDaNotificacao(DADOS)).toEqual({
      origem: { id: 1, nome: 'Sé' },
      destino: { id: 2, nome: 'Luz' },
      criterio: 'MENOR_TEMPO',
      alternativa: true,
    });
  });

  it('sem alternativa e com critério desconhecido usa o equilibrado', () => {
    const rota = rotaDaNotificacao({ ...DADOS, alternativa: 'false', criterio: 'OUTRO' });
    expect(rota?.alternativa).toBe(false);
    expect(rota?.criterio).toBe('EQUILIBRADO');
  });

  it('ignora notificações sem rota (como os lembretes locais)', () => {
    expect(rotaDaNotificacao(undefined)).toBeNull();
    expect(rotaDaNotificacao({ idAgendamento: 3 })).toBeNull();
    expect(rotaDaNotificacao({ ...DADOS, origemId: 'abc' })).toBeNull();
    expect(rotaDaNotificacao({ ...DADOS, destinoNome: ' ' })).toBeNull();
  });
});

describe('chaveDoTrajeto', () => {
  it('junta origem, destino e critério', () => {
    expect(chaveDoTrajeto('Sé', 'Luz', 'EQUILIBRADO')).toBe('Sé|Luz|EQUILIBRADO');
  });
});
