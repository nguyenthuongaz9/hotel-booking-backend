FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY target/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "-Xmx512m", "-Xms256m", "-Duser.timezone=Asia/Ho_Chi_Minh", "app.jar"]