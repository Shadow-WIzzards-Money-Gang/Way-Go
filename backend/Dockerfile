# --- Etapa 1: build do jar com Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro só o pom.xml para aproveitar o cache de dependências
# em builds futuros (só reexecuta o download se o pom mudar).
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Etapa 2: imagem final, só com o JRE e o jar gerado ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Porta padrão do Spring Boot (application.properties não sobrescreve server.port)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]