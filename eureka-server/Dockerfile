FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY webhook-service/pom.xml webhook-service/
COPY code-fetch-service/pom.xml code-fetch-service/
COPY code-analysis-service/pom.xml code-analysis-service/
COPY ai-review-service/pom.xml ai-review-service/
COPY review-storage-service/pom.xml review-storage-service/
COPY notification-service/pom.xml notification-service/

COPY common/src common/src
COPY eureka-server/src eureka-server/src

RUN mvn clean package -DskipTests -pl eureka-server -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/eureka-server/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser
EXPOSE 8761

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]