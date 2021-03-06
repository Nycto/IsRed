package com.roundeights.isred

/**
 * Methods for interacting with Redis Hashes
 */
trait Hashes extends Iface {

    /** Delete one or more hash fields */
    def hDel ( key: Key, field: Key, fields: Key* ): IntResult
        = getInt( "HDEL" ::: key :: field :: fields :: Cmd() )

    /** Determine if a hash field exists */
    def hExists ( key: Key, field: Key ): BoolResult
        = getBool( "HEXISTS" ::: key :: field :: Cmd() )

    /** Get the value of a hash field */
    def hGet[A : Convert] ( key: Key, field: Key ): OptBulkResult[A]
        = getOptBulk[A]( "HGET" ::: key :: field :: Cmd() )

    /** Get all the fields and values in a hash */
    def hGetAll[A : Convert] ( key: Key ): BulkMapResult[A]
        = getBulkMap[A]( "HGETALL" ::: key :: Cmd() )

    /** Increment the integer value of a hash field by the given number */
    def hIncrBy ( key: Key, field: Key, increment: Int ): IntResult
        = getInt( "HINCRBY" ::: key :: field :: increment :: Cmd() )

    /** Increment the float value of a hash field by the given amount */
    def hIncrByFloat ( key: Key, field: Key, increment: Double ): FloatResult
        = getFloat( "HINCRBYFLOAT" ::: key :: field :: increment :: Cmd() )

    /** Get all the fields in a hash */
    def hKeys( key: Key ): BulkSetResult[Key]
        = getBulkSet[Key]( "HKEYS" ::: key :: Cmd() )

    /** Get the number of fields in a hash */
    def hLen ( key: Key ): IntResult = getInt( "HLEN" ::: key :: Cmd() )

    /** Get the values of all the given hash fields */
    def hMGet[A : Convert] (
        key: Key, field: Key, fields: Key*
    ): BulkSeqResult[A] = {
        getBulkSeq[A]( "HMGET" ::: key :: field :: fields :: Cmd() )
    }

    /** Set multiple hash fields to multiple values */
    def hMSet (
        key: Key, pair: (Key, String), pairs: (Key, String)*
    ): AckResult
        = getAck( "HMSET" ::: key :: pair :: pairs :: Cmd() )

    /** Set the string value of a hash field */
    def hSet ( key: Key, field: Key, value: String ): AckResult
        = getAck( "HSET" ::: key :: field :: value :: Cmd() )

    /** Set the value of a hash field, only if the field does not exist */
    def hSetNX ( key: Key, field: Key, value: String ): AckResult
        = getAck( "HSETNX" ::: key :: field :: value :: Cmd() )

    /** Get all the values in a hash */
    def hVals[A : Convert] ( key: Key ): BulkSeqResult[A]
        = getBulkSeq[A]( "HVALS" ::: key :: Cmd() )

}

