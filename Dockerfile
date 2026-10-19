# Imagem da API. Etapa 1 compila com Maven; etapa 2 só roda o .jar.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Baixa as dependências antes de copiar o código, para o Docker reaproveitar
# essa camada enquanto o pom.xml não mudar.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Os testes rodam fora do Docker (o contextLoads precisa de um banco).
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
