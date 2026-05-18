FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["java","-jar","app.jar"]
