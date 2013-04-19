package com.roundeights.isred

import org.specs2.mutable._
import org.specs2.mock._

import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executor

class RedisTest extends Specification with Mockito {

    /** Blocks while waiting for the given future */
    def await[T] ( future: Future[T] ): T
        = Await.result( future, Duration(10, "second") )

    /** An execution context that runs in the calling thread */
    implicit val context = ExecutionContext.fromExecutor(new Executor {
        override def execute( command: Runnable ): Unit = command.run
    })

    /** A shared command instance */
    val cmd = Cmd("TEST")

    /** Builds a redis instance that returns the given reply */
    def newRedis ( reply: Reply ): Redis = {
        val engine = mock[Engine]
        engine.send( cmd ) returns Future.successful(reply)
        new Redis( engine )
    }


    "Reply conversion" should {

        "Build an Ack" in {
            await( newRedis( SuccessReply("OK") ).getAck(cmd) ) must_== true
        }

        "Build an Int" in {
            await( newRedis( IntReply(123) ).getInt(cmd) ) must_== 123
        }

        "Build a Float" in {
            await( newRedis( StringReply("3.14") ).getFloat(cmd) ) must_== 3.14
        }

        "Build a Boolean" in {
            await( newRedis( IntReply(0) ).getBool(cmd) ) must_== false
            await( newRedis( IntReply(1) ).getBool(cmd) ) must_== true
        }

        "Build a Bulk" in {
            await(
                newRedis( StringReply("Test") ).getBulk[String](cmd)
            ) must_== "Test"

            await(
                newRedis( StringReply("123") ).getBulk[Int](cmd)
            ) must_== 123

            await(
                newRedis( StringReply("3.14") ).getBulk[Double](cmd)
            ) must_== 3.14

            await(
                newRedis( StringReply("3.14") ).getBulk[Float](cmd)
            ) must_== 3.14f

            await( newRedis( IntReply(1) ).getBulk[Boolean](cmd) ) must_== true
        }

        "Build an Optional Bulk" in {
            await(
                newRedis( StringReply("Test") ).getOptBulk[String](cmd)
            ) must_== Some("Test")

            await(
                newRedis( NullReply() ).getOptBulk[String](cmd)
            ) must_== None
        }

        "Build a Set of Bulks" in {
            await(
                newRedis( MultiReply(
                    StringReply("One"), StringReply("Two")
                ) ).getBulkSet[String](cmd)
            ) must_== Set( "One", "Two" )
        }

        "Build a Sequence of Bulks" in {
            await(
                newRedis( MultiReply(
                    StringReply("One"), StringReply("Two")
                ) ).getBulkSeq[String](cmd)
            ) must_== Seq( "One", "Two" )
        }

        "Build a Map of Bulks" in {
            await(
                newRedis( MultiReply(
                    StringReply("One"), StringReply("Two"),
                    StringReply("Three"), StringReply("Four")
                ) ).getBulkMap[String](cmd)
            ) must_== Map( "One" -> "Two", "Three" -> "Four" )

            await(
                newRedis( MultiReply(
                    StringReply("One"), StringReply("Two"),
                    StringReply("Three")
                ) ).getBulkMap[String](cmd)
            ) must_== Map( "One" -> "Two" )
        }

        "Return a TTL result" in {
            await( newRedis( IntReply(-1) ).getTtl(cmd) ) must_== None
            await( newRedis( IntReply(-2) ).getTtl(cmd) ) must_== None
            await( newRedis( IntReply(0) ).getTtl(cmd) ) must_== Some(0)
            await( newRedis( IntReply(50) ).getTtl(cmd) ) must_== Some(50)
        }

        "Fail when a Pop command times out" in {
            await(
                newRedis( NullReply() ).getPop[String](cmd)
            ) must throwA[PopTimeout]
        }

        "Build a Pop response tuple" in {
            await(
                newRedis( MultiReply(
                    StringReply("name"), StringReply("value")
                ) ).getPop[String](cmd)
            ) must_== ("name" -> "value")
        }

        "Fail for improperly structed pop results" in {
            await(
                newRedis( MultiReply(IntReply(1)) ).getPop[String](cmd)
            ) must throwA[UnexpectedReply]
        }

    }

}

