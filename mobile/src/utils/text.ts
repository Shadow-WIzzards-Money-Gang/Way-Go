/** Remove acentos e caixa, como o backend faz em Textos.normalizar, para buscas tolerantes. */
export function normalizar(texto: string): string {
  // Remove os diacríticos combinantes (U+0300 a U+036F) que o NFD separa das letras.
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}

export function contemTexto(alvo: string, busca: string): boolean {
  const termo = normalizar(busca);
  return termo === '' || normalizar(alvo).includes(termo);
}

/** "Linha 11 - Coral" -> "Linha 11 – Coral" (traço tipográfico, como nos protótipos). */
export function formatarNomeLinha(nome: string): string {
  return nome.replace(/\s+-\s+/, ' – ');
}
