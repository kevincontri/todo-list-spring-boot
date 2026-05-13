FROM ubuntu:latest as build

RUN apt-get update && install openjdk-17-jdk -y

COPY . . 

RUN apt-get install maven -y
RUN mvn clean install -DskipTests

EXPOSE 8080

COPY --from=build /target/todolist-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
