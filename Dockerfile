FROM openjdk:12

WORKDIR /app

COPY . /app
RUN ./gradlew build && \
    ./gradlew fullJar

CMD ["java", "-jar", "./build/libs/ancillary-1.0.jar"]
