# Etapa 1: compila con Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S styleradar && adduser -S styleradar -G styleradar
COPY --from=build /app/target/gj-chat-service.jar app.jar
RUN chown styleradar:styleradar app.jar
USER styleradar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
