FROM openjdk:7-jdk-alpine

ADD sbt /usr/bin/sbt

# NOTE: does not exist compiled for Java 7, so we compile it expressly
RUN apk add --update git bash wget && rm -rf /var/cache/apk/*
RUN cd / && git clone https://github.com/gzoller/ScalaJack.git -b 4.8.3 && cd ScalaJack && git checkout -b release/4.8.3 && sbt publishLocal && cd / && rm -fR ScalaJack

WORKDIR /app
ADD build.sbt /app/
ADD project /app/project
RUN sbt update

ADD src /app/src
RUN sbt package

VOLUME /app/target

EXPOSE 8080

CMD ["sbt"]
