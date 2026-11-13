import type { Rota, StatusOperacional } from '@/types/api';

export type Tom = 'success' | 'warning' | 'danger' | 'neutral';

interface InfoStatus {
  rotulo: string;
  tom: Tom;
  /** Quanto maior, pior. Usado para agregar o status de uma rota com várias linhas. */
  severidade: number;
}

const INFO: Record<StatusOperacional, InfoStatus> = {
  NORMAL: { rotulo: 'Normal', tom: 'success', severidade: 0 },
  ATIVIDADE_PROGRAMADA: { rotulo: 'Atividade programada', tom: 'warning', severidade: 1 },
  OPERACAO_TRANSITORIA: { rotulo: 'Operação transitória', tom: 'warning', severidade: 1 },
  VELOCIDADE_REDUZIDA: { rotulo: 'Velocidade reduzida', tom: 'warning', severidade: 2 },
  OPERACAO_PARCIAL: { rotulo: 'Operação parcial', tom: 'warning', severidade: 2 },
  PARALISADA: { rotulo: 'Paralisada', tom: 'danger', severidade: 3 },
  OPERACAO_ENCERRADA: { rotulo: 'Encerrada', tom: 'danger', severidade: 3 },
  DESCONHECIDO: { rotulo: 'Sem informação', tom: 'neutral', severidade: -1 },
};

export function infoStatus(status: StatusOperacional): InfoStatus {
  return INFO[status] ?? INFO.DESCONHECIDO;
}

export function ehStatusNormal(status: StatusOperacional): boolean {
  return status === 'NORMAL' || status === 'DESCONHECIDO';
}

/** A rota passa por alguma linha fora da operação normal? (mesma regra do ?alternativa=true) */
export function rotaTemLinhaComProblema(rota: Pick<Rota, 'trechos'>): boolean {
  return rota.trechos.some((t) => !ehStatusNormal(t.statusLinha));
}

/** Status agregado de uma rota salva, como os badges de "Minhas rotas". */
export type StatusRota = {
  rotulo: 'Normal' | 'Atenção' | 'Interrompida' | 'Sem informação';
  tom: Tom;
};

export function statusDaRota(statusDasLinhas: StatusOperacional[]): StatusRota {
  if (statusDasLinhas.length === 0) return { rotulo: 'Sem informação', tom: 'neutral' };
  const pior = Math.max(...statusDasLinhas.map((s) => infoStatus(s).severidade));
  if (pior >= 3) return { rotulo: 'Interrompida', tom: 'danger' };
  if (pior >= 1) return { rotulo: 'Atenção', tom: 'warning' };
  if (pior === 0) return { rotulo: 'Normal', tom: 'success' };
  return { rotulo: 'Sem informação', tom: 'neutral' };
}
