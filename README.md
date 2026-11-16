<p align="center">
  <img src="docs/icone-way-go.png" alt="WayGo" width="96" />
</p>

<h1 align="center">WayGo</h1>

<p align="center">
  Status em tempo real do metrô e dos trens de São Paulo, cálculo de rotas que desviam de linhas
  com problema e lembretes na hora de sair de casa.
</p>

---

## Sobre o projeto

O WayGo junta em um só app o que o passageiro precisa antes de sair:

- **Status das linhas:** o backend consulta a API [Trilhos Agora](https://trilhosagora.com.br) a cada 2 minutos e guarda o histórico de cada mudança. O administrador também pode alterar o status manualmente.
- **Mapa da rede:** mostra todas as linhas e estações. Tocar numa estação abre "Sair daqui" / "Ir para cá".
- **Linhas e estações:**
  - lista de linhas com busca e filtro por tipo (metrô, trem, monotrilho);
  - detalhe da linha com status e a sequência de estações;
  - lista de estações paginada, com busca e filtro "com integração";
  - detalhe da estação com as linhas que passam por ela e alerta quando alguma tem problema.
- **Cálculo de rotas:**
  - três critérios: menor tempo, menos baldeações ou equilibrado;
  - linhas paralisadas ficam de fora e linhas lentas pesam mais no tempo;
  - opção de **rota alternativa**, que usa só linhas em operação normal.
- **Rotas salvas e agendamentos:**
  - o passageiro salva os trajetos do dia a dia;
  - agenda vários horários por rota, com dias da semana e antecedência do aviso.
- **Notificações push (FCM):**
  - lembrete antes da hora de sair, dizendo se a rota está ok ou qual é a alternativa;
  - alerta quando muda o status de uma linha de uma rota salva;
  - ao tocar na notificação, o app abre a rota no mapa.
- **Conta:**
  - cadastro e login com JWT;
  - "Minha conta", com alteração de senha, encerrar sessão e exclusão da conta;
  - recuperação de senha por código de 6 dígitos enviado por e-mail.
- **E-mails:** boas-vindas no cadastro, código de recuperação, aviso de senha redefinida e confirmação de conta excluída.
- **Área do administrador:**
  - lista de usuários;
  - cadastro e edição de linhas, com status manual e ordem das estações;
  - cadastro e edição de estações, com posição no mapa.

| Mapa | Rota calculada | Status das linhas | Rotas salvas |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-mapa.jpeg" width="180" /> | <img src="docs/telas/tela-mapa-rota.jpeg" width="180" /> | <img src="docs/telas/tela-linhas.jpeg" width="180" /> | <img src="docs/telas/tela-rotas-salvas.jpeg" width="180" /> |

Todas as telas estão em [Telas](#telas).

**Documentação:**
- [Backlog do produto](docs/backlog-do-produto.md): épicos, histórias e critérios de aceite.
- [Diagrama de classes](docs/diagrama-de-classes.md): domínio do backend.
- [Diagrama entidade-relacionamento](docs/diagrama-entidade-relacionamento.md): tabelas do banco.

## Telas

### Mapa e rotas

| Rede inteira | Estação tocada no mapa | Rota calculada | Rota com linha com problema |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-mapa.jpeg" width="180" /> | <img src="docs/telas/tela-mapa-estacao.jpeg" width="180" /> | <img src="docs/telas/tela-mapa-rota.jpeg" width="180" /> | <img src="docs/telas/tela-mapa-rota-com-problema.jpeg" width="180" /> |

### Linhas e estações

| Status das linhas | Detalhe da linha | Estações | Detalhe da estação |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-linhas.jpeg" width="180" /> | <img src="docs/telas/tela-detalhe-linha.jpeg" width="180" /> | <img src="docs/telas/tela-estacoes.jpeg" width="180" /> | <img src="docs/telas/tela-detalhe-estacao.jpeg" width="180" /> |

| Estação com linha em alerta |
|:---:|
| <img src="docs/telas/tela-detalhe-estacao-alerta.jpeg" width="180" /> |

### Rotas salvas e agendamentos

| Minhas rotas | Sem rotas salvas | Sem login | Salvar rota |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-rotas-salvas.jpeg" width="180" /> | <img src="docs/telas/tela-rotas-vazia.jpeg" width="180" /> | <img src="docs/telas/tela-rotas-sem-login.jpeg" width="180" /> | <img src="docs/telas/tela-salvar-rota.jpeg" width="180" /> |

| Editar rota | Novo horário |
|:---:|:---:|
| <img src="docs/telas/tela-editar-rota.jpeg" width="180" /> | <img src="docs/telas/tela-agendamento.jpeg" width="180" /> |

### Conta

| Login | Cadastro | Esqueci minha senha | Redefinir senha |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-login.jpeg" width="180" /> | <img src="docs/telas/tela-cadastro.jpeg" width="180" /> | <img src="docs/telas/tela-esqueci-senha.jpeg" width="180" /> | <img src="docs/telas/tela-redefinir-senha.jpeg" width="180" /> |

| Redefinir senha (preenchida) | Minha conta | Alterar senha | Encerrar sessão |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-redefinir-senha-preenchida.jpeg" width="180" /> | <img src="docs/telas/tela-conta.jpeg" width="180" /> | <img src="docs/telas/tela-alterar-senha.jpeg" width="180" /> | <img src="docs/telas/tela-conta-encerrar-sessao.jpeg" width="180" /> |

| Excluir conta | Menu sem login | Menu logado |
|:---:|:---:|:---:|
| <img src="docs/telas/tela-conta-excluir.jpeg" width="180" /> | <img src="docs/telas/tela-mapa-menu-visitante.jpeg" width="180" /> | <img src="docs/telas/tela-mapa-menu-conta.jpeg" width="180" /> |

### Administrador

| Painel | Usuários | Editar linha e status | Ordem das estações |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-admin.jpeg" width="180" /> | <img src="docs/telas/tela-admin-usuarios.jpeg" width="180" /> | <img src="docs/telas/tela-admin-editar-linha.jpeg" width="180" /> | <img src="docs/telas/tela-admin-editar-linha-ordem-estacoes.jpeg" width="180" /> |

| Nova linha | Editar estação | Nova estação | Conta do administrador |
|:---:|:---:|:---:|:---:|
| <img src="docs/telas/tela-admin-nova-linha.jpeg" width="180" /> | <img src="docs/telas/tela-admin-editar-estacao.jpeg" width="180" /> | <img src="docs/telas/tela-admin-nova-estacao.jpeg" width="180" /> | <img src="docs/telas/tela-conta-admin.jpeg" width="180" /> |

### E-mails

| Boas-vindas | Código de recuperação | Senha redefinida |
|:---:|:---:|:---:|
| <img src="docs/telas/email-boas-vindas.jpeg" width="180" /> | <img src="docs/telas/email-recuperacao-senha.jpeg" width="180" /> | <img src="docs/telas/email-senha-redefinida.jpeg" width="180" /> |


## Tecnologias

| Parte | Stack |
|---|---|
| **Backend** ([`backend/`](backend)) | Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA / Hibernate, Flyway, OpenFeign, Spring Mail, Firebase Admin (FCM), springdoc (Swagger) |
| **Banco** | Oracle Database 23 Free |
| **Mobile** ([`mobile/`](mobile)) | React Native + Expo SDK 58 (expo-router), TypeScript, NativeWind, TanStack Query, Zustand, react-native-maps, expo-notifications |
| **Infra** | Docker / Docker Compose, EAS Build (APK), Render (backend hospedado) |

O backend segue arquitetura hexagonal:

```
backend/src/main/java/com/bytestorm/waygo
├── domain/          # entidades, regras e cálculo de rotas (sem dependência de framework)
├── application/     # serviços (casos de uso), DTOs, eventos e caches
├── infrastructure/  # JPA, segurança JWT, cliente da API Trilhos Agora, e-mail, push (FCM) e agendadores
└── presentation/    # controllers REST e tratamento de erros
```

As convenções do app estão em [mobile/AGENTS.md](mobile/AGENTS.md).

## Pré-requisitos

| Ferramenta | Versão | Para quê |
|---|---|---|
| [Git](https://git-scm.com) | qualquer | Clonar o repositório |
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | 24+ (com Docker Compose v2) | Banco Oracle e, na opção 1, o projeto inteiro |
| [Java JDK](https://adoptium.net) | **21+** | Rodar o backend fora do Docker. O Maven não precisa ser instalado: o projeto traz o Maven Wrapper (`mvnw`) |
| [Node.js](https://nodejs.org) | **20+** (o contêiner usa a 22), com **npm** | Rodar o app fora do Docker |
| **Expo Go** ([Android](https://play.google.com/store/apps/details?id=host.exp.exponent) / [iOS](https://apps.apple.com/app/expo-go/id982107779)) | compatível com o SDK 58 | Abrir o app no celular durante o desenvolvimento |
| [Android Studio](https://developer.android.com/studio) | opcional | Emulador Android |
| Conta [Expo](https://expo.dev) + EAS CLI (`npx eas-cli@latest`) | opcional | Gerar o APK (necessário para o push) |

Contas e chaves de serviços externos, todas opcionais (sem elas o projeto roda, só sem o recurso correspondente):

| Serviço | Variável | Sem ela |
|---|---|---|
| [Trilhos Agora](https://trilhosagora.com.br) | `TRILHOS_AGORA_API_KEY` | Os status não são atualizados automaticamente (só pelo admin) |
| Gmail (ou outro SMTP) com [senha de app](https://myaccount.google.com/apppasswords) | `MAIL_USERNAME` / `MAIL_PASSWORD` | Os e-mails e o código de recuperação de senha aparecem só no log do backend |
| [Firebase](https://console.firebase.google.com) | `FCM_CREDENCIAIS` + `mobile/google-services.json` | As notificações push aparecem só no log, e o app usa os avisos locais |
| [Google Maps SDK for Android](https://console.cloud.google.com/google/maps-apis) | `GOOGLE_MAPS_API_KEY` | Só faz falta no build do APK (no Expo Go o mapa funciona sem ela) |

## Como rodar

Clone o projeto:

```bash
git clone <url-do-repositorio>
cd WayGo
```

### Opção 1: tudo com Docker (recomendado)

```bash
cp .env.example .env                   # defina HOST_IP com o IP da sua máquina na rede local
cp backend/.env.example backend/.env   # opcional: chaves da Trilhos Agora, e-mail, JWT, Firebase...
docker compose up -d --build
```

Isso sobe três contêineres:

| Serviço | Porta | O que é |
|---|---|---|
| `banco` | 1521 | Oracle Free (usuário `app` / `app123`). **Na primeira vez leva alguns minutos** para inicializar |
| `backend` | 8080 | API. Reinicia sozinho até o banco ficar pronto e então roda as migrations, que criam as tabelas e carregam a rede |
| `mobile` | 8081 | Servidor do Expo (Metro) |

Para abrir o app:

- **No celular:** rode `docker compose logs -f mobile` e leia o QR code com o Expo Go (Android) ou com a câmera (iOS). O celular precisa estar na mesma rede Wi-Fi do computador.
- **No navegador:** acesse <http://localhost:8081> (na web o mapa não é exibido).
- **No emulador Android:** abra o Expo Go no emulador e digite `exp://<HOST_IP>:8081`.

Comandos úteis:

```bash
docker compose logs -f backend   # acompanhar a API
docker compose restart mobile    # recarregar o Metro
docker compose down              # parar tudo (os dados do banco se perdem ao remover o contêiner)
```

> **Dica:** no Windows, as alterações no código do app nem sempre são detectadas dentro do contêiner. Se o app não atualizar, rode `docker compose restart mobile`, ou rode o app fora do Docker (opção 2) enquanto desenvolve.

### Opção 2: rodando cada parte na máquina

```bash
# 1. Banco (só ele no Docker)
docker compose up -d banco

# 2. Backend (http://localhost:8080)
cd backend
cp .env.example .env
./mvnw spring-boot:run          # no Windows: mvnw.cmd spring-boot:run

# 3. App (em outro terminal)
cd mobile
cp .env.example .env
npm install
npx expo start                  # "a" abre no emulador Android; ou leia o QR code com o Expo Go
```

Com o backend no ar, a documentação interativa da API fica em <http://localhost:8080/swagger-ui.html>.

Mais detalhes do app (emulador, backend hospedado e problemas comuns) em [mobile/README.md](mobile/README.md).

### Gerar o APK (Android)

O push só funciona no APK: no Expo Go e no iOS, o app usa as notificações locais.

1. Coloque o `google-services.json` do Firebase em `mobile/` (veja [Notificações push](#notificações-push-fcm)).
2. Defina `GOOGLE_MAPS_API_KEY` no `mobile/.env`, ou cadastre-a no EAS com `eas env:create --name GOOGLE_MAPS_API_KEY --environment preview --visibility sensitive`. Sem ela, o build é interrompido, porque o APK fecharia ao abrir o mapa.
3. Gere o build:

```bash
cd mobile
npx eas-cli@latest login
npx eas-cli@latest build -p android --profile preview
```

O perfil `preview` (`mobile/eas.json`) aponta o app para o backend hospedado (`https://way-go.onrender.com`). Para usar outro servidor, troque `EXPO_PUBLIC_API_HOST` nesse perfil.

## Configuração

Os arquivos `.env` ficam fora do git. Cada um tem um `.env.example` versionado como modelo: copie e preencha.

### Raiz (`.env`), usado pelo Docker Compose

| Variável | Obrigatória | Descrição |
|---|---|---|
| `HOST_IP` | Para usar no celular | IP da máquina na rede local (Windows: `ipconfig`, "Endereço IPv4"; Linux/macOS: `ip addr`). O Expo Go usa esse endereço para baixar o app e acessar a API. Sem ele, só o navegador e o emulador funcionam |

### Backend (`backend/.env`)

Todas têm valor padrão: para rodar localmente, basta copiar o `.env.example`. No Docker Compose, as variáveis do banco já são definidas pelo `docker-compose.yml`.

| Variável | Padrão | Descrição |
|---|---|---|
| **Servidor e banco** | | |
| `PORT` | `8080` | Porta da API |
| `CORS_ORIGENS_PERMITIDAS` | `*` | Origens liberadas para o app web |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `1521` / `FREEPDB1` | Banco Oracle (no Docker Compose o host é `banco`) |
| `DB_USERNAME` / `DB_PASSWORD` | `app` / `app123` | Usuário do banco |
| **Status das linhas** | | |
| `TRILHOS_AGORA_URL` | `https://api.trilhosagora.com.br/v1` | URL da API Trilhos Agora |
| `TRILHOS_AGORA_API_KEY` | *(vazio)* | **Preencha** para atualizar os status automaticamente |
| `TRILHOS_AGORA_INTERVALO` | `PT2M` | Intervalo entre as consultas (ISO-8601) |
| `TRILHOS_AGORA_AGENDAMENTO` | `true` | Liga ou desliga as consultas automáticas |
| `TRILHOS_AGORA_RECARGA_CACHE` | `PT30M` | Recarga do cache de status a partir do banco |
| **Cálculo de rotas** | | |
| `ROTAS_CRITERIO_PADRAO` | `EQUILIBRADO` | Critério quando a requisição não informa (`MENOR_TEMPO`, `MENOS_BALDEACOES`, `EQUILIBRADO`) |
| `ROTAS_PENALIDADE_BALDEACAO` | `PT3M` | Custo extra por baldeação no critério equilibrado |
| `ROTAS_FATOR_LENTIDAO` | `1.5` | Multiplicador do tempo em linhas lentas ou com operação parcial |
| `ROTAS_RECARGA_GRAFO` | `PT6H` | De quanto em quanto tempo o grafo da rede é remontado |
| **Autenticação** | | |
| `JWT_SEGREDO` | `segredo` | Segredo de assinatura dos tokens. **Troque em produção** por um valor longo e aleatório |
| `JWT_EXPIRACAO_MINUTOS` | `60` | Validade do token |
| `JWT_EMISSOR` | `waygo` | Emissor do token |
| **E-mail** | | |
| `EMAIL_HABILITADO` | `false` | Envia os e-mails de verdade. Desligado, o conteúdo (inclusive o código de recuperação) vai para o log |
| `MAIL_HOST` / `MAIL_PORT` | `smtp.gmail.com` / `587` | Servidor SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | *(vazio)* | **Preencha** com `EMAIL_HABILITADO=true`. No Gmail, use uma [senha de app](https://myaccount.google.com/apppasswords), não a senha normal |
| `MAIL_REMETENTE` / `MAIL_NOME_REMETENTE` | `MAIL_USERNAME` / `WayGo` | Endereço e nome do remetente |
| **Notificações push** | | |
| `FCM_HABILITADO` | `false` | Envia os push pelo Firebase. Desligado, eles aparecem só no log. **Ligado sem `FCM_CREDENCIAIS` válidas, o backend não sobe** |
| `FCM_CREDENCIAIS` | *(vazio)* | **Preencha** com `FCM_HABILITADO=true`: conta de serviço do Firebase como caminho do JSON, o próprio JSON ou o JSON em base64 (recomendado em Render/Docker) |
| `NOTIFICACOES_AGENDAMENTO` | `true` | Liga ou desliga as rotinas de lembretes e de envio da fila |
| `NOTIFICACOES_INTERVALO_LEMBRETES` | `PT1M` | Verificação dos lembretes dos agendamentos |
| `NOTIFICACOES_INTERVALO_FILA` | `PT30S` | Envio da fila de notificações (e das novas tentativas) |

### App (`mobile/.env`)

Só as variáveis com prefixo `EXPO_PUBLIC_` chegam ao app, e ficam visíveis no bundle: nunca coloque segredos nelas. Depois de alterar o `.env`, reinicie o Metro com `npx expo start -c`.

| Variável | Padrão | Descrição |
|---|---|---|
| `EXPO_PUBLIC_API_PROTOCOL` | `http` | `http` ou `https` |
| `EXPO_PUBLIC_API_HOST` | *(vazio)* | Host da API. Vazio usa o IP da máquina do Metro (ideal para celular físico). No emulador Android, use `10.0.2.2`. Servidor hospedado: só o domínio (`way-go.onrender.com`) ou a URL completa |
| `EXPO_PUBLIC_API_PORT` | `8080` | Porta da API. Em servidor hospedado, deixe vazia |
| `EXPO_PUBLIC_API_PREFIX` | `/api/v1` | Prefixo das rotas da API |
| `EXPO_PUBLIC_API_TIMEOUT_MS` | `10000` | Tempo máximo por requisição. Com o Render gratuito, que "dorme", use `60000` |
| `EXPO_PUBLIC_STATUS_REFRESH_MS` | `120000` | Intervalo de atualização automática do status |
| `GOOGLE_MAPS_API_KEY` | *(vazio)* | Chave do Google Maps SDK for Android. Usada só no build do APK (`app.config.js`) |

## Usuário administrador

A migration `V7` cria um administrador para testes:

| E-mail | Senha |
|---|---|
| `admin@gmail.com` | `Admin@123` |

Ao entrar com essa conta, o app mostra a aba **Admin**. Não há tela para promover contas: para tornar outra conta administradora, adicione a role `ADMINISTRADOR` na tabela `USUARIO_ROLE`. Administradores e moderadores não podem excluir a própria conta.

## API

Com o backend rodando, a documentação interativa fica em <http://localhost:8080/swagger-ui.html>. Todas as rotas têm o prefixo `/api/v1`.

| Recurso | Endpoints | Acesso |
|---|---|---|
| Autenticação | `POST /auth/registro`, `POST /auth/login` | Público |
| Recuperação de senha | `POST /auth/esqueci-minha-senha` (envia código de 6 dígitos por e-mail), `POST /auth/redefinir-senha` (e-mail + código + nova senha) | Público |
| Rede | `GET /rede` (linhas, estações, conexões e status em uma chamada) | Público |
| Linhas | `GET /linhas`, `GET /linhas/{id}`, `GET /linhas/{id}/estacoes`, `GET /linhas/{id}/status` | Público |
| Estações | `GET /estacoes?pagina&tamanho&busca&integracoes` (paginado), `GET /estacoes/{id}`, `/{id}/linhas`, `/{id}/conexoes` | Público |
| Cálculo de rota | `GET /rotas/calcular?origem&destino&criterio&alternativa` (origem e destino pelo nome da estação) | Público |
| Rotas salvas | `GET`, `POST /rotas` · `GET`, `PUT`, `DELETE /rotas/{id}` | Logado (só as próprias) |
| Agendamentos | `GET /rotas/agendamentos` · `GET`, `POST /rotas/{idRota}/agendamentos` · `GET`, `PUT`, `DELETE /rotas/agendamentos/{id}` | Logado (só os próprios) |
| Minha conta | `GET /usuarios/eu` · `POST /usuarios/eu/atualizar-senha` · `DELETE /usuarios/eu` (exclui a conta e envia e-mail de confirmação) | Logado |
| Aparelhos (push) | `POST /dispositivos` (token FCM, plataforma e alertas de status ligados ou não) · `DELETE /dispositivos?token=` | Logado |
| Usuários | `GET /usuarios?pagina&tamanho` | Administrador |
| Linhas (admin) | `POST /linhas` · `PUT`, `DELETE /linhas/{id}` · `PUT /linhas/{id}/estacoes/ordem` · `PATCH /linhas/{id}/status` (status manual) | Administrador |
| Estações (admin) | `POST /estacoes` · `PUT`, `DELETE /estacoes/{id}` | Administrador |

Nos endpoints protegidos, envie o token no header `Authorization: Bearer <token>`.

**Recuperação de senha:**
- O código vale por 15 minutos, aceita 5 tentativas e só pode ser usado uma vez.
- Pedir um novo código invalida o anterior, e um novo envio só sai 1 minuto depois do último.
- `esqueci-minha-senha` sempre responde `202`, exista ou não a conta, para não revelar quais e-mails estão cadastrados.
- Com `EMAIL_HABILITADO=false`, o código aparece no log do backend.

## Notificações push (FCM)

O backend manda dois tipos de push pelo Firebase Cloud Messaging:

- **Lembrete do agendamento (US11):** no horário de aviso (partida menos a antecedência), diz se a rota está ok e quanto tempo leva, ou quais linhas têm problema e qual é a rota alternativa.
- **Alerta de status (US15 e US16):**
  - quando muda o status de uma linha usada por uma rota salva, avisa quem está com os alertas ligados, já com a rota alternativa quando houver;
  - ao tocar no aviso, o app abre a rota no mapa, na alternativa se ela foi oferecida.

**Histórico, fila e reenvio:**
- Cada notificação fica gravada na tabela `NOTIFICACAO`, que funciona como histórico e como fila.
- A chave única por usuário impede que a mesma notificação saia duas vezes.
- Se o envio falhar, ela é tentada de novo com espera crescente, de 30 s até 15 min, até dar certo ou expirar. O lembrete vale até o horário de partida, e o alerta de status, por 1 hora.
- Os aparelhos ficam em `DISPOSITIVO_USUARIO`. Um token que o Firebase recusa é desativado.

**Configurar:**

1. Crie um projeto no [console do Firebase](https://console.firebase.google.com) e adicione um app Android com o pacote `com.bytestorm.waygo`.
2. Baixe o `google-services.json` para `mobile/`. O `app.config.js` só o usa se o arquivo existir.
3. Em *Configurações do projeto > Contas de serviço*, gere uma chave privada e informe em `FCM_CREDENCIAIS`, com `FCM_HABILITADO=true`.
4. Gere o APK (veja [Gerar o APK](#gerar-o-apk-android)).

O `google-services.json` e a chave privada não vão para o git. O `.easignore` só libera o `google-services.json` para o build do EAS.

Com o push ativo, o app desliga os avisos locais para não duplicar. Ao sair da conta, o aparelho é removido.

## Testes

```bash
cd backend && ./mvnw test        # testes unitários do backend

cd mobile
npm test                         # testes unitários (Jest)
npm run typecheck                # TypeScript
npm run lint                     # ESLint
```
