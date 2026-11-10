const { colors } = require('./src/theme/colors');

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  presets: [require('nativewind/preset')],
  // O app só tem tema claro; 'class' evita o erro do NativeWind na web ao trocar o esquema de cores.
  darkMode: 'class',
  theme: {
    extend: {
      colors,
      borderRadius: {
        card: '16px',
      },
    },
  },
  plugins: [],
};
