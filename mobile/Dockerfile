# Servidor de desenvolvimento do Expo (Metro). O app roda no celular (Expo Go),
# no emulador ou no navegador; este contêiner só serve o bundle JavaScript.
FROM node:22-bookworm-slim

WORKDIR /app
ENV EXPO_NO_TELEMETRY=1

# Instala as dependências antes de copiar o código para aproveitar o cache
# (só reinstala quando package.json ou package-lock.json mudam).
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Porta do Metro (bundle para o Expo Go e versão web)
EXPOSE 8081

CMD ["npx", "expo", "start", "--lan", "--port", "8081"]
