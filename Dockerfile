# Build environment for iron-cache-play2 (Play 2.1 / Scala 2.10 / Maven).
#
# Scala 2.10 needs a Java 8 runtime, so the image pins JDK 8 rather than a
# modern LTS. Nothing has to be installed on the host except Docker.
#
#   docker build -t iron-cache-play2 .
#   docker run --rm iron-cache-play2                 # runs `mvn package`
#   docker run --rm -it iron-cache-play2 bash        # interactive shell
#
# Mount ~/.m2 to reuse downloaded dependencies between runs:
#   docker run --rm -v "$HOME/.m2:/root/.m2" iron-cache-play2

FROM maven:3.9-eclipse-temurin-8

WORKDIR /app

# Resolve dependencies and plugins in their own layer so source edits do not
# trigger a full re-download.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline || true

COPY app ./app
COPY test ./test

CMD ["mvn", "-B", "-DskipTests", "package"]
