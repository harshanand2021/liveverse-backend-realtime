# Build stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Runtime stage
FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24
ENV LANGUAGE='en_US:en'

COPY --chown=185 --from=builder /app/build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder /app/build/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder /app/build/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder /app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080

USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
