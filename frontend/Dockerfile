FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]