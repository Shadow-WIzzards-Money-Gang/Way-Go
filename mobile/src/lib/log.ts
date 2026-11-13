/**
 * Logs de desenvolvimento. Aparecem no terminal do Metro (`npx expo start`) e são
 * desligados no build de produção (__DEV__ = false). Usa console.log também para falhas,
 * de propósito: console.warn/console.error abririam o LogBox por cima da tela a cada
 * erro de rede, que o app já trata e mostra ao usuário.
 */
const ativo = __DEV__;

function escrever(nivel: 'INFO' | 'ERRO', escopo: string, mensagem: string, extra: unknown[]) {
  if (!ativo) return;
  const hora = new Date().toISOString().slice(11, 23);
  console.log(`${hora} ${nivel} [${escopo}] ${mensagem}`, ...extra);
}

export const log = {
  info: (escopo: string, mensagem: string, ...extra: unknown[]) =>
    escrever('INFO', escopo, mensagem, extra),
  erro: (escopo: string, mensagem: string, ...extra: unknown[]) =>
    escrever('ERRO', escopo, mensagem, extra),
};
