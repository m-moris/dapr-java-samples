FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]