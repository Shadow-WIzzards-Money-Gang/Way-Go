import { useEffect, useState } from 'react';

/** Devolve `valor` só depois de `atrasoMs` sem mudanças (debounce), p.ex. para buscas na API. */
export function useValorAtrasado<T>(valor: T, atrasoMs = 350): T {
  const [atrasado, setAtrasado] = useState(valor);

  useEffect(() => {
    const timer = setTimeout(() => setAtrasado(valor), atrasoMs);
    return () => clearTimeout(timer);
  }, [valor, atrasoMs]);

  return atrasado;
}
