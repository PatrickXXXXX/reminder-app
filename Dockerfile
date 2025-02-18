FROM eclipse-temurin:17-jre
WORKDIR /app
COPY build/libs/reminder-app-1.0-SNAPSHOT.jar /app/reminder-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "reminder-app.jar"]
