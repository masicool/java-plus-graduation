FROM amazoncorretto:21-alpine
COPY target/*.jar stats-server.jar
ENTRYPOINT ["java", "-jar", "/stats-server.jar"]
