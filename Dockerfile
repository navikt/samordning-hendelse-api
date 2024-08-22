FROM navikt/java:17
USER root
RUN apt-get update && apt-get install -y \
  curl \
  && rm -rf /var/lib/apt/lists/*
USER apprunner
RUN curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
COPY .nais/opentelemetry.sh /init-scripts/
COPY build/libs/hendelser-1.jar /app/app.jar
