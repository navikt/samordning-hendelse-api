FROM ghcr.io/navikt/baseimages/temurin:21

COPY build/libs/hendelser-1.jar /app/app.jar
