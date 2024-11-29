FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM oraclelinux:8-slim
COPY --from=build /target/forge-0.0.1-SNAPSHOT.jar flavor-forge.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","flavor-forge.jar"]