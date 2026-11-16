import type { CriterioRota } from '@/types/api';

const CRITERIOS: readonly CriterioRota[] = ['MENOR_TEMPO', 'MENOS_BALDEACOES', 'EQUILIBRADO'];

/** Rota que o push do backend pede para abrir no mapa ao ser tocado. */
export interface RotaDaNotificacao {
  origem: { id: number; nome: string };
  destino: { id: number; nome: string };
  criterio: CriterioRota;
  /** true quando a notificação ofereceu uma rota alternativa (evitando linhas com problema). */
  alternativa: boolean;
}

/**
 * Lê o campo "data" do push (texto para texto, como o FCM manda).
 * Devolve null quando a notificação não traz uma rota (ex.: lembretes locais).
 */
export function rotaDaNotificacao(dados: Record<string, unknown> | null | undefined): RotaDaNotificacao | null {
  if (!dados) return null;
  const texto = (chave: string) => {
    const valor = dados[chave];
    return typeof valor === 'string' || typeof valor === 'number' ? String(valor).trim() : '';
  };
  const origemId = Number(texto('origemId'));
  const destinoId = Number(texto('destinoId'));
  const origemNome = texto('origemNome');
  const destinoNome = texto('destinoNome');
  if (!Number.isInteger(origemId) || !Number.isInteger(destinoId) || origemId <= 0 || destinoId <= 0) {
    return null;
  }
  if (!origemNome || !destinoNome) return null;

  const criterio = CRITERIOS.find((c) => c === texto('criterio')) ?? 'EQUILIBRADO';
  return {
    origem: { id: origemId, nome: origemNome },
    destino: { id: destinoId, nome: destinoNome },
    criterio,
    alternativa: texto('alternativa') === 'true',
  };
}

/** Identifica o trajeto escolhido no mapa (a escolha de "rota alternativa" vale só para ele). */
export function chaveDoTrajeto(origem: string, destino: string, criterio: CriterioRota): string {
  return `${origem}|${destino}|${criterio}`;
}
