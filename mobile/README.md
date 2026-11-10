# WayGo Mobile

App mobile do WayGo (React Native + Expo + NativeWind). Mostra o status das linhas de metrô e trem, as estações de cada linha, calcula rotas no mapa e guarda suas rotas favoritas com lembretes.

| Tela | O que faz | API |
|---|---|---|
| **Mapa** | Origem/destino, rota sugerida com trechos, baldeações e avisos; critério (equilibrado, menor tempo, menos baldeações) | `GET /rotas`, `GET /rede` |
| **Status** | Status de todas as linhas, alerta quando uma rota salva é afetada | `GET /rede` |
| **Linhas** | Lista e filtro por tipo; detalhe com timeline de estações e baldeações | `GET /rede`, `GET /linhas/{id}`, `GET /linhas/{id}/status` |
| **Rotas** | Rotas salvas (no aparelho), agendamento de lembrete e alertas | — |

## Requisitos

- Node 20+
- Backend rodando (`../backend`, porta padrão 8080)
- App **Expo Go** no celular, ou um emulador Android/iOS

## Como rodar

```bash
cd mobile
npm install
cp .env.example .env     # ajuste se necessário
npx expo start
```

Leia o QR code com o Expo Go (o celular precisa estar na mesma rede do computador), ou pressione `a` para abrir no emulador Android.

## Configuração (.env)

| Variável | Padrão | Descrição |
|---|---|---|
| `EXPO_PUBLIC_API_PROTOCOL` | `http` | `http` ou `https` |
| `EXPO_PUBLIC_API_HOST` | *(vazio)* | Host da API. Vazio usa o IP da máquina do Metro (ideal para celular físico). No emulador Android, use `10.0.2.2` em vez de `localhost` |
| `EXPO_PUBLIC_API_PORT` | `8080` | Porta da API (`PORT` do backend) |
| `EXPO_PUBLIC_API_PREFIX` | `/api/v1` | Prefixo das rotas (`WebConfig.API_PREFIXO`) |
| `EXPO_PUBLIC_API_TIMEOUT_MS` | `10000` | Tempo máximo de espera por requisição |
| `EXPO_PUBLIC_STATUS_REFRESH_MS` | `120000` | Intervalo de atualização automática do status |

Depois de alterar o `.env`, reinicie o Metro com `npx expo start -c`.

### Problemas comuns

- **"Não foi possível conectar ao servidor" no navegador:** o backend precisa liberar CORS para a origem do app (`CORS_ORIGENS_PERMITIDAS`, por padrão `http://localhost:*`). Na web o mapa não aparece; use o Expo Go para a experiência completa.
- **Mesmo erro no celular físico:** confira se o celular está na mesma rede Wi-Fi do PC e se o Firewall do Windows permite conexões de entrada na porta 8080.

## Scripts

```bash
npm run lint        # ESLint
npm run typecheck   # TypeScript
npm test            # testes unitários (Jest)
npm run format      # Prettier
```

As convenções de código e a arquitetura estão em [AGENTS.md](./AGENTS.md).
