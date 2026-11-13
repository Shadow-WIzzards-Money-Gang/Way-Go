/** Remove acentos e caixa, como o backend faz em Textos.normalizar, para buscas tolerantes. */
export function normalizar(texto: string): string {
  // Remove os diacríticos combinantes (U+0300 a U+036F) que o NFD separa das letras.
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}

export function contemTexto(alvo: string, busca: string): boolean {
  const termo = normalizar(busca);
  return termo === '' || normalizar(alvo).includes(termo);
}

// Comparadores criados uma única vez. No Hermes, `a.localeCompare(b, 'pt-BR')` monta um
// Intl.Collator novo a cada chamada: ordenar centenas de estações assim trava a thread JS.
const COLLATOR_NOMES = new Intl.Collator('pt-BR', { sensitivity: 'base' });
const COLLATOR_CODIGOS = new Intl.Collator('pt-BR', { numeric: true });

/** Ordem alfabética em português ("Água Branca" antes de "Brás"). Use em `.sort()`. */
export const compararNomes = (a: string, b: string): number => COLLATOR_NOMES.compare(a, b);

/** Ordem numérica de códigos de linha ("2" antes de "11"). Use em `.sort()`. */
export const compararCodigos = (a: string, b: string): number => COLLATOR_CODIGOS.compare(a, b);

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
