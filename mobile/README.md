# WayGo Mobile

App mobile do WayGo (React Native + Expo + NativeWind). Mostra o status das linhas de metrô e trem, as estações de cada linha, calcula rotas no mapa e guarda suas rotas favoritas com lembretes.

| Tela | O que faz | API |
|---|---|---|
| **Mapa** | Origem/destino, rota sugerida com trechos, baldeações e avisos; critério (equilibrado, menor tempo, menos baldeações) | `GET /rotas/calcular`, `GET /rede` |
| **Estações** | Busca e filtro de todas as estações; detalhe com linhas, status, integração e atalhos "Sair daqui"/"Ir para cá" | `GET /rede`, `GET /estacoes/{id}` |
| **Linhas** | Lista e filtro por tipo; detalhe com timeline de estações e baldeações | `GET /rede`, `GET /linhas/{id}`, `GET /linhas/{id}/status` |
| **Rotas** | Rotas salvas na conta (exige login), lembrete no aparelho e alertas | `GET/POST/PUT/DELETE /rotas` |
| **Conta** | Login, cadastro e "Minha conta", pelo avatar no canto das telas | `POST /auth/login`, `POST /auth/registro`, `GET /usuarios/me` |

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
- **Backend hospedado (ex.: Render) não responde:** use `EXPO_PUBLIC_API_PROTOCOL=https`, `EXPO_PUBLIC_API_HOST=way-go.onrender.com` (sem `https://`) e `EXPO_PUBLIC_API_PORT` **vazia**: o serviço responde na porta 443, não na 8080. O plano gratuito do Render "dorme" e leva até ~1 min para acordar, então use um `EXPO_PUBLIC_API_TIMEOUT_MS` alto (60000). Reinicie com `npx expo start -c`.
- **Para ver o que o app está chamando:** os logs aparecem no terminal do Metro. Ao abrir o app sai a `URL base`, e cada requisição aparece com `→ Buscando GET /rede`, `← 200 GET /rede (120 ms)` ou `✕ Sem resposta ... URL completa: ...`.
- **Mesmo erro no celular físico:** confira se o celular está na mesma rede Wi-Fi do PC e se o Firewall do Windows permite conexões de entrada na porta 8080.

## Scripts

```bash
npm run lint        # ESLint
npm run typecheck   # TypeScript
npm test            # testes unitários (Jest)
npm run format      # Prettier
```

As convenções de código e a arquitetura estão em [AGENTS.md](./AGENTS.md).
