# Etapa 1: build do projeto com Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom e baixa dependências (melhora cache)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copia o código fonte e builda
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: imagem leve só com o JRE e o jar
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia o jar gerado da etapa de build
COPY --from=build /app/target/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# (Opcional) Porta de runtime vinda de env, com fallback pra 8080
ENV SERVER_PORT=8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
