# API image. Stage 1 builds with Maven; stage 2 just runs the .jar.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Downloads dependencies before copying the source, so Docker can reuse
# this layer as long as pom.xml doesn't change.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Tests run outside Docker (contextLoads needs a database).
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
