IsRed [![Build Status](https://secure.travis-ci.org/Nycto/IsRed.png?branch=master)](http://travis-ci.org/Nycto/IsRed)
=====

A non-blocking, asynchronous Redis client for Scala

Adding it to your Project
-------------------------

This project is not currently hosted on any publicly available maven
repositories. However, you can still add it to your project by publishing it to
your local repository.

Run the following commands:

```
git clone https://github.com/Nycto/IsRed.git /tmp/IsRed;
cd /tmp/IsRed;
sbt publish-local;
```

Then, just add this to your `build.sbt` file and recompile:

```
libraryDependencies ++= Seq(
    "com.roundeights" %% "isred" % "0.2"
)
```

Usage
-----

Once you have this library added to your build configuration as a dependency,
using it is simple:

```scala
package com.example

import com.roundeights.isred.Redis
import scala.concurrent.ExecutionContext.Implicits.global

object Example extends App {

    // Spin up a new connection pool
    val redis = new Redis(
        host = "localhost",
        port = 6379,
        maxConnect = 5
    )

    // Set a value in redis, then immediately read the value back
    redis.set("myKey", "some value")
        .flatMap( _ => redis.get[String]("myKey") )
        .onComplete { case value => {
            println( value )
            redis.shutdown
        }}
}
```

In general, every Redis operation maps to a function on the Redis class.
Requests are made asynchronously, which means that each function returns a
Future.

License
-------

This project is released under the MIT License, which is pretty spiffy. You
should have received a copy of the MIT License along with this program. If not,
see <http://www.opensource.org/licenses/mit-license.php>.

