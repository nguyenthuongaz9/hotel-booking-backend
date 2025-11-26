# Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY hotel_service/pom.xml ./hotel_service/pom.xml

RUN mvn -f ./hotel_service/pom.xml dependency:go-offline -DskipTests

COPY hotel_service ./hotel_service
RUN mvn -f ./hotel_service/pom.xml clean package -DskipTests

# Extract
FROM eclipse-temurin:17-jre-alpine as extractor
WORKDIR /app
COPY --from=builder /app/hotel_service/target/*.jar app.jar
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

EXPOSE 8084
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-Duser.timezone=Asia/Ho_Chi_Minh", "org.springframework.boot.loader.JarLauncher"]