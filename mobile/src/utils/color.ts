const COR_PADRAO = { r: 100, g: 116, b: 139 }; // ink.muted

/** "#1266af" / "#FFF" -> { r, g, b }, ou null se não for um hex válido. */
function hexParaRgb(hex: string | null | undefined): { r: number; g: number; b: number } | null {
  if (!hex) return null;
  const limpo = hex.trim().replace(/^#/, '');
  const completo =
    limpo.length === 3
      ? limpo
          .split('')
          .map((c) => c + c)
          .join('')
      : limpo;
  if (!/^[0-9a-fA-F]{6}$/.test(completo)) return null;
  const valor = Number.parseInt(completo, 16);
  return { r: (valor >> 16) & 255, g: (valor >> 8) & 255, b: valor & 255 };
}

/** Escolhe texto claro ou escuro para ficar legível sobre a cor da linha (ex.: Linha 4 amarela). */
export function corDeTextoSobre(hex: string): '#FFFFFF' | '#0F172A' {
  const rgb = hexParaRgb(hex);
  if (!rgb) return '#FFFFFF';
  // Luminância percebida (ITU-R BT.601).
  const luminancia = (0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b) / 255;
  return luminancia > 0.62 ? '#0F172A' : '#FFFFFF';
}

/**
 * Cor sempre válida para props nativas (react-native-maps). Uma cor inválida vira null
 * no Android e derruba o app, então caímos numa cor neutra. Saída em rgba().
 */
export function corSegura(hex: string | null | undefined, opacidade = 1): string {
  const { r, g, b } = hexParaRgb(hex) ?? COR_PADRAO;
  const alfa = Math.min(1, Math.max(0, opacidade));
  return `rgba(${r}, ${g}, ${b}, ${alfa})`;
}
