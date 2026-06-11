FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -pl etrs-order-service -am

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/etrs-order-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]