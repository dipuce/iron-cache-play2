Iron Cache module for Play Framework
===

A [Play Framework][play] cache module backed by [IronCache][iron] from Iron.io. It binds
Play's standard `AsyncCacheApi` / `SyncCacheApi` (Scala and Java) to Iron Cache and exposes
the Iron-specific extras (atomic increment/decrement, clear, list caches) through
`com.dipuce.cache.iron.IronCacheApi`.

Requirements
---

* Play 3.0.x
* Scala 2.13 or Scala 3 (cross-published for both)
* Java 17 or 21
* [Iron.io][iron] credentials

> Looking for the Play 2.1–2.3 / Scala 2.10 plugin? Use version `3.0.1`
> (`"com.dipuce" %% "iron-cache-play2" % "3.0.1"`), the last release of that line.
> Play removed the plugin API it was built on in 2.4, so 4.x is a rewrite against the
> module/DI API. The artifact keeps its historical `iron-cache-play2` name.

Usage
---

Add the dependency to your Play project:

```scala
libraryDependencies += "com.dipuce" %% "iron-cache-play2" % "4.0.0"
```

The module enables itself. Configure your credentials in `conf/application.conf`:

```hocon
# Only needed if another cache module (ehcache, caffeine) is also on the classpath:
# play.modules.disabled += "play.api.cache.ehcache.EhCacheModule"

iron {
  token      = "<your Iron.io token>"
  project.id = "<your Iron.io project id>"

  # Optional; these are the defaults.
  cache {
    host    = "https://cache-aws-us-east-1.iron.io"
    name    = "cache"
    timeout = 5 seconds   # request timeout, and the await timeout of SyncCacheApi
  }
}
```

Then inject a cache as usual:

```scala
import javax.inject.Inject
import scala.concurrent.duration._
import play.api.cache.AsyncCacheApi
import com.dipuce.cache.iron.IronCacheApi

class MyService @Inject() (cache: AsyncCacheApi, iron: IronCacheApi) {
  cache.set("greeting", "hello", 1.hour)
  cache.get[String]("greeting")            // Future[Option[String]]

  iron.increment("hits")                   // Future[Option[Long]], atomic on the server
  iron.decrement("stock", 3)
  iron.clear()                             // same as removeAll()
  iron.listCaches()                        // Future[Map[cacheName, projectId]]
}
```

Iron Cache stores JSON, so values must be JSON-representable: `String`, `Boolean`, any
numeric primitive, `BigDecimal`, or a play-json `JsValue`. Anything else fails the returned
future with an `IllegalArgumentException`. Unexpected responses from Iron.io fail with an
`IronCacheException(status, message)`.

Building
---

The build is sbt and cross-compiles for every supported Scala version:

```sh
sbt +test          # run the tests on Scala 2.13 and Scala 3
sbt +publishLocal  # publish both artifacts to ~/.ivy2/local
sbt sample/run     # start the sample app (needs IRON_TOKEN and IRON_PROJECT_ID)
```

No local Scala or sbt? Use the Dockerfile:

```sh
docker build -t iron-cache-play2 .
docker run --rm iron-cache-play2                     # sbt +test
docker run --rm iron-cache-play2 sbt +publishLocal
```

Mount `-v iron-cache-coursier:/root/.cache/coursier -v iron-cache-sbt:/root/.sbt` to keep
downloaded dependencies between runs.

Tests run offline against an in-process stub of the Iron Cache REST API
(`src/test/scala/.../IronCacheStub.scala`), so no credentials are needed.

Sample
---

`sample/` is a minimal Play application wired to the module. Set `IRON_TOKEN` and
`IRON_PROJECT_ID`, run `sbt sample/run`, and hit the routes in `sample/conf/routes`.

License
---

Apache License 2.0. See `LICENSE`.

[play]: https://www.playframework.com/
[iron]: https://www.iron.io/
