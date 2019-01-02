FROM navikt/java:11
FROM library/postgres:11
COPY target/app.jar /app/app.jar