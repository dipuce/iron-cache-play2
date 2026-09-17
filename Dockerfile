# Build environment for iron-cache-play2 (Play 3 / Scala 2.13 + 3 / sbt).
#
# Nothing has to be installed on the host except Docker.
#
#   docker build -t iron-cache-play2 .
#   docker run --rm iron-cache-play2                    # cross-builds and tests every Scala version
#   docker run --rm iron-cache-play2 sbt +publishLocal  # publish both artifacts to ~/.ivy2/local
#   docker run --rm -it iron-cache-play2 bash           # interactive shell
#
# Reuse downloaded dependencies between runs by mounting the caches:
#   docker run --rm -v iron-cache-coursier:/root/.cache/coursier \
#                   -v iron-cache-sbt:/root/.sbt iron-cache-play2

FROM sbtscala/scala-sbt:eclipse-temurin-17.0.20_8_1.13.0_3.3.8

WORKDIR /app

# Resolve sbt, plugins and library dependencies in their own layer so that
# source edits do not trigger a full re-download.
COPY build.sbt ./
COPY project/build.properties project/plugins.sbt ./project/
RUN sbt -batch "+update" "sample/update"

COPY src ./src
COPY sample ./sample

CMD ["sbt", "-batch", "+test"]
