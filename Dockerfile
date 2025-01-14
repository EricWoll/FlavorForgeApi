FROM openjdk:21-jdk-slim as build
WORKDIR /app
COPY src .
RUN ./mvnw clean package -DskipTests


FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/forge-0.0.1-SNAPSHOT.jar flavor-forge.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","flavor-forge.jar"]