# Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY api_gateway/pom.xml ./api_gateway/pom.xml

RUN mvn -f ./api_gateway/pom.xml dependency:go-offline -DskipTests

COPY api_gateway ./api_gateway
RUN mvn -f ./api_gateway/pom.xml clean package -DskipTests

# Extract
FROM eclipse-temurin:17-jre-alpine as extractor
WORKDIR /app
COPY --from=builder /app/api_gateway/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=extractor /app/dependencies/ ./ 
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

EXPOSE 8900
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-Duser.timezone=Asia/Ho_Chi_Minh", "org.springframework.boot.loader.JarLauncher"]
