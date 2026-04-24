FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /app/target/desafio-0.0.1-SNAPSHOT.jar desafio.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "desafio.jar"]