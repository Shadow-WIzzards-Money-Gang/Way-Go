/** Remove acentos e caixa, como o backend faz em Textos.normalizar, para buscas tolerantes. */
export function normalizar(texto: string): string {
  // Remove os diacríticos combinantes (U+0300 a U+036F) que o NFD separa das letras.
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}

export function contemTexto(alvo: string, busca: string): boolean {
  const termo = normalizar(busca);
  return termo === '' || normalizar(alvo).includes(termo);
}

/** "ana@email.com" -> "A". Usa o primeiro caractere alfanumérico; sem nenhum, "?". */
export function inicialDoEmail(email: string | null | undefined): string {
  const caractere = normalizar(email ?? '').match(/[\p{L}\p{N}]/u)?.[0];
  return caractere ? caractere.toUpperCase() : '?';
}

/** Validação simples de formato, só para orientar o usuário antes de chamar a API. */
export function ehEmailValido(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

/** "Linha 11 - Coral" -> "Linha 11 – Coral" (traço tipográfico, como nos protótipos). */
export function formatarNomeLinha(nome: string): string {
  return nome.replace(/\s+-\s+/, ' – ');
}
