FROM openjdk:8-jdk-alpine

RUN apk add --update bash wget && rm -rf /var/cache/apk/*
ADD sbt /usr/bin/sbt

WORKDIR /app
ADD build.sbt /app/
ADD project /app/project
RUN sbt update

ADD src /app/src
RUN sbt package

VOLUME /app/target

EXPOSE 8080

CMD ["sbt"]
