FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -ntp -DskipTests package

FROM eclipse-temurin:17-jre-alpine

ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /app/target/flood-rescue-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

