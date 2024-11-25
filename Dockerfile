FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ="Europe/Oslo"
COPY build/libs/hendelser-1.jar /app/app.jar

CMD ["-jar", "/app/app.jar"]