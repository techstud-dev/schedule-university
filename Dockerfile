FROM amazoncorretto:21 AS BUILDER
MAINTAINER mr.nikamilon
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline


COPY src ./src
RUN ./mvnw clean package -Pdev -DskipTests

FROM  amazoncorretto:21-alpine
WORKDIR /app

COPY --from=builder app/target/schedule-university-1.0.0-SNAPSHOT.jar /app

RUN apk add --no-cache curl

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "schedule-university-1.0.0-SNAPSHOT.jar"]