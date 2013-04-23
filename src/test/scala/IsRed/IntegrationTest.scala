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

    "Key operations" should {

        "Support existence ops: EXISTS, DEL" in {
            val key = "test-key-exist"
            await( redis.set(key, "value") )
            await( redis.exists(key) ) must_== true
            await( redis.del(key) )
            await( redis.exists(key) ) must_== false
        }

        "Support key ops: RANDOMKEY, KEYS, KEYTYPE" in {
            val key = "test-key-keys"
            await( redis.set(key, "value") )
            await( redis.randomKey() ) must beAnInstanceOf[Key]
            await( redis.keys("test-key-*") ).length must be_>(0)
            await( redis.keyType(key) ) must_== KeyType.STRING
        }

        "Support rename ops: RENAME, RENAMENX" in {
            val key = "test-key-rename"
            val key2 = "test-key-rename-2"
            await( redis.set(key, "value") )
            await( redis.rename(key, key2) ) must_== true
            await( redis.rename(key2, key) ) must_== true
        }

        "Support expire ops: EXPIRE, EXPIREAT, PERSIST, PEXPIRE, PEXPIREAT" in {
            val key = "test-key-expire"
            await( redis.set(key, "value") )
            await( redis.expire(key, 600) ) must_== true
            await( redis.persist( key ) ) must_== true
            await( redis.expireAt(
                key, (System.currentTimeMillis / 1000L).toInt + 600
            ) ) must_== true
            await( redis.pExpire(key, 6000) ) must_== true
            await( redis.pExpireAt(
                key, System.currentTimeMillis.toInt + 6000
            ) ) must_== true
        }

        "Support ttl ops: TTL, PTTL" in {
            val key = "test-key-ttl"
            await( redis.set(key, "value") )
            await( redis.expire(key, 600) )
            await( redis.ttl(key) ).get must be_>(0)
            await( redis.pTtl(key) ).get must be_>(0)
        }

        "Support dump ops: DUMP, RESTORE" in {
            val key = "test-key-dump"
            val into = "test-key-into"
            await( redis.del(into) )
            await( redis.set(key, "value") )
            val dumped = await( redis.dump(key) )
            await( redis.restore( into, dumped ) ) must_== true
            await( redis.get[String](into) ) must_== Some("value")
        }
    }

    "Hash operations" should {

        "Support basic ops: HSET, HGET, HDEL, HEXISTS, HLEN" in {
            val key = "test-hash-basic"
            await( redis.del(key) )
            await( redis.hSet(key, "key", "value") ) must_== true
            await( redis.hGet[String](key, "key") ) must_== Some("value")
            await( redis.hExists(key, "key") ) must_== true
            await( redis.hLen(key) ) must_== 1
            await( redis.hDel(key, "key") ) must_== 1
            await( redis.hLen(key) ) must_== 0
        }

        "Support list ops: HKEYS, HVALS" in {
            val key = "test-hash-list"
            await( redis.del(key) )
            await( redis.hSet(key, "key1", "value1") )
            await( redis.hSet(key, "key2", "value2") )
            await( redis.hKeys(key) ) must_== Set[Key]("key1", "key2")
            await( redis.hVals[String](key) ) must_== Seq("value1", "value2")
        }

        "Support bulk ops: HMGET, HMSET, HGETALL" in {
            val key = "test-hash-bulk"
            await( redis.del(key) )
            await( redis.hMSet(
                key, "key1" -> "val1", "key2" -> "val2", "key3" -> "val3"
            ) ) must_== true
            await( redis.hMGet[String](key, "key1", "key2") ) must_==
                Seq( "val1", "val2" )
            await( redis.hGetAll[String](key) ) must_==
                Map( "key1" -> "val1", "key2" -> "val2", "key3" -> "val3" )
        }

        "Support increment ops: HINCRBY, HINCRBYFLOAT" in {
            val key = "test-hash-inc"
            await( redis.del(key) )
            await( redis.hSet(key, "key", "0") )
            await( redis.hIncrBy(key, "key", 1) ) must_== 1
            await( redis.hIncrByFloat(key, "key", 3.14) ) must_== 4.14
        }

        "Support nonexisting ops: HSETNX" in {
            val key = "test-hash-notexist"
            await( redis.del(key) )
            await( redis.hSetNX(key, "key", "value") ) must_== true
            await( redis.hSetNX(key, "key", "value") ) must
                throwA[UnsuccessfulReply]
        }

    }

}

