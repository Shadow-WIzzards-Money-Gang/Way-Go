# AGENTS.md — WayGo Mobile

App Expo/React Native do WayGo: status das linhas de metrô/trem de São Paulo, estações por linha, cálculo de rotas no mapa e rotas salvas. Consome a API Spring Boot em `../backend`. Os protótipos de referência ficam em `../docs/telas/`.

Priorize padrões mobile-first, desempenho e compatibilidade entre Android e iOS.

## O Expo mudou: não confie na memória

O Expo quebra APIs a cada SDK. Antes de escrever código que use uma API do Expo, EAS ou React Native:

1. Veja a versão major de `expo` no `package.json` (hoje **SDK 58**).
2. Consulte a documentação dessa versão: `https://docs.expo.dev/versions/v<major>.0.0/`
3. Para o resto, use https://docs.expo.dev/llms.txt (índice da documentação com correções de equívocos comuns de LLMs).

Pontos específicos desta versão que já confirmamos:
- `Tabs` vem de `expo-router/js-tabs` e `Stack` de `expo-router/stack`. Importar `Tabs` direto de `expo-router` está deprecado.
- `initialRouteName` de um navegador é definido por `export const unstable_settings = { initialRouteName: '...' }` no `_layout.tsx`.
- NativeWind **4.2.x** com Tailwind **3.4.x** (não use Tailwind v4 nem NativeWind v5 sem migrar tudo).
- `react-native-gesture-handler` é a v3. Por isso evitamos `@gorhom/bottom-sheet`: o painel do mapa é um componente próprio.

## Comandos

```bash
npm install                 # instala dependências
cp .env.example .env        # configura a API (veja "Ambiente")
npx expo start              # servidor de desenvolvimento (Expo Go / emulador)
npm run lint                # ESLint (eslint-config-expo + React Compiler)
npm run typecheck           # tsc --noEmit
npm test                    # Jest (jest-expo)
npm run format              # Prettier + ordenação de classes Tailwind
npx expo install <pacote>   # SEMPRE use este comando para adicionar dependências (resolve versões do SDK)
npx expo-doctor             # diagnóstico de dependências e configuração
```

**Antes de dar uma tarefa por concluída, rode `npm run lint`, `npm run typecheck` e `npm test`.** Para mudanças de navegação ou de dependências nativas, rode também `npx expo export --platform android` para garantir que o bundle compila.

## Ambiente (.env)

- Toda variável fica em `.env` (fora do git), com `.env.example` como modelo versionado. Ao criar uma variável, atualize os dois.
- Só variáveis com prefixo `EXPO_PUBLIC_` chegam ao app, e **ficam visíveis no bundle**. Nunca coloque segredos nelas.
- Leia variáveis **apenas** em `src/config/env.ts`, que as valida com zod e expõe `env.apiBaseUrl`, `env.apiTimeoutMs` e `env.statusRefreshMs`. Nenhum outro arquivo usa `process.env`.
- Com `EXPO_PUBLIC_API_HOST` vazio, o app usa o IP da máquina do Metro (funciona em celular físico). `localhost` não funciona no emulador Android: use `10.0.2.2`.

## Arquitetura

```
src/
  app/                  # rotas do Expo Router (cada arquivo é uma tela)
    _layout.tsx         # providers (React Query, tema) + Stack raiz e modais
    (tabs)/             # Status · Linhas · Mapa (index) · Rotas
    (tabs)/linhas/      # lista de linhas + [id] (timeline de estações)
    selecionar-estacao.tsx, rota-salva.tsx   # modais
  api/                  # cliente axios, ApiError e funções tipadas por endpoint
  config/env.ts         # leitura e validação do .env
  types/api.ts          # espelho dos DTOs Java do backend
  hooks/                # queries do React Query e hooks de domínio
  store/                # estado do cliente (zustand): planejamento e rotas salvas (AsyncStorage)
  services/             # integrações do dispositivo (notificações)
  components/           # componentes de domínio
  components/ui/        # primitivos visuais (Button, Card, Badge, Chip, Skeleton, States...)
  theme/colors.js       # paleta única, usada pelo Tailwind e pelo JS
  utils/                # funções puras (formatação, status, texto, cor), com testes em __tests__
```

Fluxo de dados: **tela → hook (`src/hooks/queries.ts`) → função da API (`src/api/waygo.ts`) → `api` (axios)**.

## Regras

### TypeScript
- Use `strict` e não use `any`. Use `unknown` com estreitamento de tipo quando necessário.
- Os tipos da API ficam em `src/types/api.ts` e espelham os records de `backend/.../application/dto`. Se um DTO mudar no backend, atualize o tipo aqui no mesmo PR.
- Use o alias `@/` para `src/`. Nada de imports relativos longos (`../../..`).

### Dados e API
- Somente `src/api/` usa o axios. Telas e componentes consomem dados por hooks do React Query.
- As chaves de query ficam centralizadas em `queryKeys` (`src/hooks/queries.ts`).
- Os erros chegam como `ApiError` (tipos `rede`, `timeout`, `nao-encontrado`, `sem-rota`, `invalido`, `servidor`). Trate pelo `tipo`, não pelo texto. O `ErroResponse` do backend vem em `message`.
- Para o status das linhas, prefira `/rede`: uma chamada traz todas as linhas com `status`. Evite N chamadas a `/linhas/{id}/status`.
- `/rotas` recebe **nomes** de estação (origem/destino), não ids.

### UI/UX
- Estilize apenas com classes NativeWind e os tokens do `tailwind.config.js` (`bg-background`, `text-ink-muted`, `bg-primary`, `bg-warning-soft`...). Não use hex soltos. Quando a cor precisa estar em JS (ícones, mapa), importe de `@/theme/colors`. A cor das linhas vem da API (`linha.cor`) e vai em `style`.
- Não use `className` em componentes do Reanimated (`Animated.View`). Envolva com um `View` estilizado e anime só via `style`.
- Toda tela que busca dados tem estados de **carregando** (Skeleton, não spinner de tela cheia), **erro** (`ErrorState` com "Tentar novamente") e **vazio** (`EmptyState`).
- Listas usam `FlatList` com `keyExtractor` e itens com `memo`. Use pull-to-refresh onde o dado muda (status).
- Acessibilidade: `accessibilityRole` e `accessibilityLabel` em todo `Pressable` e ícone sem texto, alvos de toque de pelo menos 44px e `accessibilityRole="header"` nos títulos.
- Os textos da interface ficam em **português do Brasil**. Nomes de linha passam por `formatarNomeLinha` ("Linha 11 – Coral").
- Siga os protótipos de `../docs/telas/`: cards brancos `rounded-2xl` sobre fundo `#F5F7FB`, primária azul, badges verde/amarelo/vermelho.

### Componentes
- Mantenha componentes pequenos e focados. Primitivos genéricos vão em `components/ui/` e componentes de domínio em `components/`.
- Componentes não buscam dados: recebem props. Quem busca é a tela, pelos hooks.
- O React Compiler está ativo. Não adicione `useMemo`/`useCallback` sem necessidade, e se o lint acusar `preserve-manual-memoization`, extraia a lógica para uma função pura.

### Testes
- Funções puras em `src/utils` e o mapeamento de erros em `src/api` têm testes em `__tests__/`. Ao mudar essas regras, atualize os testes.
- Não coloque testes dentro de `src/app/`: todo arquivo ali vira rota.

### Git
- Siga o padrão de commits do repositório: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`, com a mensagem em português.
- Nunca versione `.env`, `node_modules/`, `ios/` ou `android/` (pastas geradas via CNG; configure pelo `app.json` e por config plugins).

## Builds

- Expo Go cobre o desenvolvimento: `react-native-maps`, notificações locais e AsyncStorage já vêm nele.
- Builds de loja usam EAS (`npx eas-cli@latest build`). Em produção, use `https` na API. Para Google Maps no Android em build próprio, configure a chave em `app.json` (`android.config.googleMaps.apiKey`).

## Pendências conhecidas (TODO)

- **Rotas salvas só no aparelho** (`src/store/rotasSalvas.ts`, AsyncStorage). Quando o backend tiver endpoint, troque por queries/mutations em `src/api`.
- **"Relatar" na tela da linha** mostra um aviso "em breve": falta endpoint no backend.
- **Alertas de mudança de status** só disparam com o app aberto (comparação a cada atualização de `/rede`). Para receber em segundo plano, é preciso push no backend.
- O mapa não roda na web (`MapaRede.web.tsx` mostra um aviso no lugar).
