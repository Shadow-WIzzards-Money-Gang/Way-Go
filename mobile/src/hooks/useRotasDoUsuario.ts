import { useQueries } from '@tanstack/react-query';
import { useEffect } from 'react';

import { rotasApi } from '@/api/waygo';
import { type Agendamento, dadosLocaisDa, useRotasLocais } from '@/store/rotasLocais';
import type { LinhaDaEstacao, RotaParams, RotaSalvaResponse } from '@/types/api';
import { linhasDaRota } from '@/utils/rota';

import { queryKeys, useRotasSalvas } from './queries';

/** Rota salva da conta (API) + o que só o aparelho guarda (linhas e lembrete). */
export interface RotaDoUsuario extends RotaSalvaResponse {
  linhas: LinhaDaEstacao[];
  agendamento: Agendamento | null;
}

const paramsDa = (rota: RotaSalvaResponse): RotaParams => ({
  origem: rota.origem.nome,
  destino: rota.destino.nome,
  criterio: rota.criterio,
});

/**
 * Rotas salvas do usuário logado. Quando uma rota não tem as linhas no aparelho
 * (ex.: foi salva em outro celular), calcula o trajeto uma vez e guarda as linhas.
 */
export function useRotasDoUsuario() {
  const query = useRotasSalvas();
  const porRota = useRotasLocais((s) => s.porRota);
  const definir = useRotasLocais((s) => s.definir);

  const salvas = query.data ?? [];
  const semLinhas = salvas.filter((r) => dadosLocaisDa(porRota, r.id).linhas.length === 0);

  const calculos = useQueries({
    queries: semLinhas.map((rota) => ({
      queryKey: queryKeys.rota(paramsDa(rota)),
      queryFn: () => rotasApi.calcular(paramsDa(rota)),
      staleTime: 1000 * 60,
      retry: false,
    })),
  });

  const calculadas = calculos.map((c, i) => ({ rotaId: semLinhas[i]?.id, linhas: linhasDaRota(c.data) }));

  useEffect(() => {
    for (const { rotaId, linhas } of calculadas) {
      if (rotaId !== undefined && linhas.length > 0) definir(rotaId, { linhas });
    }
  }, [calculadas, definir]);

  const rotas: RotaDoUsuario[] = salvas.map((rota) => ({ ...rota, ...dadosLocaisDa(porRota, rota.id) }));

  return { ...query, rotas };
}
