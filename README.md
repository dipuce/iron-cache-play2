Iron Cache Plugin for Play 2.x
===

Requirements
---

* Tested with [Play 2.1.x][play]
* [Iron.io][iron] credentials

Usage
---

Add the following dependency to your Play project:

```scala
  val appDependencies = Seq(
    "com.dipuce" %% "iron-cache-play2" % "2.0.0"
  )
```
or
```
    <dependency>
        <groupId>com.dipuce</groupId>
        <artifactId>iron-cache-play2</artifactId>
        <version>2.0.0</version>
    </dependency>
```

Build
---

To build from source, clone this repo and then build this project using SBT.

    git clone https://github.com/dipuce/iron-cache-play2.git iron-cache
    cd iron-cache

    mvn package
    cp plugin/target/scala-2.10/iron-cache-play2_2.10-2.0.0-SNAPSHOT.jar <play project dir>/lib

Setup
---

To use, first the default cache has to be disabled. Then your specific iron.io credentials must also be added.
To do that, open the `application.conf` file and add a property:

    # Disable Default Cache
    ehcacheplugin=disabled

    #Iron Cache Properties

    # Required. Get these settings from the iron.io dashboard
    iron.token      = "<Your iron.io token>"
    iron.project.id = "<Your iron.io project's ID>"

    # Optional. If not specified, these values will be used instead.
    iron.cache.host = "https://cache-aws-us-east-1.iron.io"
    iron.cache.name = "cache"
    iron.cache.timeout = 5 // API timeout configured in seconds

The plugin must then be activated by adding a line to the `play.plugins` file. If one has not be created yet, create one
in the conf folder of your Play application. Add this line:

    1501:com.dipuce.cache.iron.IronCachePlugin

How to Use
---

The standard cache interface built into Play is now enabled and can be used normally.

```scala
    # Get a value
    Cache.get("key")

    # Set a value, set a value with an expiration time in milliseconds
    Cache.set("key", "value")
    Cache.set("key", "value", 3600)

    # Remove an item from the cache
    Cache.remove("key")
```
In addition, Iron Cache has a few more capabilities built into its API. To use those:

```scala
    import com.dipuce.cache.iron.IronCachePlugin
    
    val app = play.api.Play.current
    val ironPlugin = app.plugin[IronCachePlugin].get
    val cacheAPI = ironPlugin.provider.api

    # Increment an integer value
    cacheAPI.increment("key", amount_to_increment)
    
    # Decrement an integer value
    cacheAPI.decrement("key", amount_to_decrement)
    
    # List all caches
    cacheAPI.listCaches()

    # Delete all items from the cache
    cacheAPI.clear()
```

Version
---

2.0.0 Move from old repository. Complete rewrite for testability. First functional tests written.

1.0 First Stable Version. Added Maven repository for easy use.

0.2 Added LOTS of error checking. Implemented and tested the Iron specific functions.

0.1 Initial Version. Consider it very rough (no error checking).

Contact
---

If you like this plugin and want to contribute, feel free to submit a pull request!

[play]: http://www.playframework.com/ "Play Framework"
[iron]: http://www.iron.io            "Iron.io"