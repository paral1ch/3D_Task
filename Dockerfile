FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml pom.xml
RUN mvn -B -DskipTests dependency:go-offline

COPY src src
RUN mvn -B -DskipTests package
RUN cp "$(find target -maxdepth 1 -name '*.jar' ! -name '*original*' | head -n 1)" /app/application.jar

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/application.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
