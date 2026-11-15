/**
 * Complementa o app.json com valores que não podem ir para o git.
 * GOOGLE_MAPS_API_KEY vem do .env (local) ou das variáveis do EAS (build na nuvem).
 * Sem a chave, o mapa fecha o app em builds próprios no Android (no Expo Go ela já vem embutida).
 */
if (process.env.EAS_BUILD_PLATFORM === 'android' && !process.env.GOOGLE_MAPS_API_KEY) {
  throw new Error(
    'GOOGLE_MAPS_API_KEY ausente no build do EAS: o APK fecharia ao abrir o mapa. ' +
      'Defina a chave no .env (enviado pelo .easignore) ou com "eas env:create".',
  );
}

module.exports = ({ config }) => ({
  ...config,
  android: {
    ...config.android,
    config: {
      ...config.android?.config,
      googleMaps: {
        apiKey: process.env.GOOGLE_MAPS_API_KEY,
      },
    },
  },
});
