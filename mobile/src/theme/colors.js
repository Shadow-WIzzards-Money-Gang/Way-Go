/**
 * Paleta do WayGo (baseada nos protótipos em docs/telas).
 * Fonte única de cores: usada pelo tailwind.config.js e por componentes que precisam
 * da cor em JS (ícones, mapa, tab bar). Não use hex soltos nos componentes.
 */
const colors = {
  background: '#F5F7FB',
  surface: '#FFFFFF',
  border: '#E5E9F0',
  ink: {
    DEFAULT: '#0F172A',
    muted: '#64748B',
    subtle: '#94A3B8',
  },
  primary: {
    DEFAULT: '#2563EB',
    pressed: '#1D4ED8',
    soft: '#EAF1FF',
  },
  success: { DEFAULT: '#15803D', soft: '#DCFCE7' },
  warning: { DEFAULT: '#B45309', soft: '#FEF3C7', border: '#FCD9A8', bg: '#FFF7ED' },
  danger: { DEFAULT: '#DC2626', soft: '#FEE2E2' },
  neutral: { DEFAULT: '#475569', soft: '#F1F5F9' },
};

module.exports = { colors };
