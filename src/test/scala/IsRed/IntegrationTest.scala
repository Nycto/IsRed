package com.roundeights.isred

import org.specs2.mutable._
import java.net.Socket
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

class IntegrationTest extends Specification {

    /** Blocks while waiting for the given future */
    def await[T] ( future: Future[T] ): T = {
        Await.result( future, Duration(10, "second") )
    }


    // Try to connect port 7537. We use a different port than the redis default
    // to make sure that running the integration tests is an explicit decision
    try {
        val socket = new Socket("localhost", 7357);
        socket.close
    } catch {
        case e: Throwable => {
            println("Could not find a Redis server running on port 7357")
            args(skipAll = true)
        }
    }

    lazy val redis = new Redis("localhost", 7357)


    "Strings operations" should {

        "Support basic ops: GET, SET, GETSET, APPEND, STRLEN" in {
            val key = "test-strings-get-set"
            await( redis.del(key) )
            await( redis.get(key) ) must_== None
            await( redis.set(key, "value") ) must_== true
            await( redis.get[String](key) ) must_== Some("value")
            await( redis.getSet[String](key, "newVal") ) must_== Some("value")
            await( redis.append(key, "++") ) must_== 8
            await( redis.get[String](key) ) must_== Some("newVal++")
            await( redis.strLen(key) ) must_== 8
        }

        "Support multi ops: MSET, MGET" in {
            await( redis.mSet(
                "test-strings-one" -> "one",
                "test-strings-two" -> "two"
            ) ) must_== true

            await( redis.mGet[String](
                "test-strings-one", "test-strings-two"
            ) ) must_== Seq("one", "two")
        }

        "Support numeric ops: INCR, DECR, INCRBY, DECRBY, INCRBYFLOAT" in {
            val key = "test-strings-intops"
            await( redis.set(key, "10") )
            await( redis.incr(key) ) must_== 11
            await( redis.decr(key) ) must_== 10
            await( redis.incrBy(key, 5) ) must_== 15
            await( redis.decrBy(key, 3) ) must_== 12
            await( redis.incrByFloat(key, 3.1415) ) must_== 15.1415
        }

        "Support bit ops: SETBIT, GETBIT, BITCOUNT" in {
            val key = "test-strings-bitops"
            await( redis.del(key) )
            await( redis.setBit(key, 6, true) ) must_== false
            await( redis.setBit(key, 1, true) ) must_== false
            await( redis.get[String](key) ) must_== Some("B")
            await( redis.getBit(key, 1) ) must_== true
            await( redis.bitCount(key) ) must_== 2
            await( redis.bitCount(key, 0, 3) ) must_== 2
        }

        "Support bit ops: BITOP" in {
            def key( int: Int ) = "test-strings-bitops-%d".format( int )
            await( redis.del(key(1)) )
            await( redis.del(key(2)) )
            await( redis.setBit(key(1), 6, true) ) must_== false
            await( redis.setBit(key(2), 1, true) ) must_== false
            await( redis.bitOp(
                BitwiseOp.AND, key(3), key(1), key(2)
            ) ) must_== 1
        }

        "Support range ops: SETRANGE, GETRANGE" in {
            val key = "test-strings-rangeops"
            await( redis.set(key, "abcdefg") )
            await( redis.getRange[String](key, 1, 4) ) must_== "bcde"
            await( redis.setRange(key, 2, "xyz") ) must_== 7
            await( redis.get[String](key) ) must_== Some("abxyzfg")
        }

        "Support expire ops: PSETEX, SETEX" in {
            await( redis.setEx("test-strings-ex", 5, "value") ) must_== true
            await( redis.pSetEx("test-strings-ex2", 5, "value") ) must_== true
        }

        "Support not-exist op: MSETNX, SETNX" in {
            val key = "test-strings-notexist"
            await( redis.del(key) )
            await( redis.setNX(key, "value") ) must_== true
            await( redis.setNX(key, "value") ) must throwA[UnsuccessfulReply]

            await( redis.mSetNX(
                key -> "value", (key + "2") -> "value"
            ) ) must throwA[UnsuccessfulReply]
        }
    }

}

