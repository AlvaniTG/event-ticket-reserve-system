FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -pl etrs-core -am

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/etrs-core/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]