package com.roundeights.isred

/**
 * Methods for interacting with Redis Hashes
 */
trait Hashes extends Iface {

    /** Delete one or more hash fields */
    def hDel ( key: Key, field: Key, fields: Key* ): IntResult
        = getInt( Cmd("HDEL") ::: key :: field :: fields :: Cmd() )

    /** Determine if a hash field exists */
    def hExists ( key: Key, field: Key ): BoolResult
        = getBool( Cmd("HEXISTS") ::: key :: field :: Cmd() )

    /** Get the value of a hash field */
    def hGet ( key: Key, field: Key ): OptBulkResult
        = getOptBulk( Cmd("HGET") ::: key :: field :: Cmd() )

    /** Get all the fields and values in a hash */
    def hGetAll ( key: Key ): BulkMapResult
        = getBulkMap( Cmd("HGETALL") ::: key :: Cmd() )

    /** Increment the integer value of a hash field by the given number */
    def hIncrBy ( key: Key, field: Key, increment: Int ): IntResult
        = getInt( Cmd("HINCRBY") ::: key :: field :: increment :: Cmd() )

    /** Increment the float value of a hash field by the given amount */
    def hIncrByFloat ( key: Key, field: Key, increment: Float ): FloatResult
        = getFloat( Cmd("HINCRBYFLOAT") ::: key :: field :: increment :: Cmd() )

    /** Get all the fields in a hash */
    def hKeys ( key: Key ): BulkSetResult
        = getBulkSet( Cmd("HKEYS") ::: key :: Cmd() )

    /** Get the number of fields in a hash */
    def hLen ( key: Key ): IntResult
        = getInt( Cmd("HLEN") ::: key :: Cmd() )

    /** Get the values of all the given hash fields */
    def hMGet ( key: Key, field: Key, fields: Key* ): BulkMapResult
        = getBulkMap( Cmd("HMGET") ::: key :: field :: fields :: Cmd() )

    /** Set multiple hash fields to multiple values */
    def hMSet (
        key: Key, pair: (Key, String), pairs: (Key, String)*
    ): AckResult
        = getAck( Cmd("HMSET") ::: key :: pair :: pairs :: Cmd() )


    /** Set the string value of a hash field */
    def hSet ( key: Key, field: Key, value: String ): AckResult
        = getAck( Cmd("HSET") ::: key :: field :: value :: Cmd() )

    /** Set the value of a hash field, only if the field does not exist */
    def hSetNX ( key: Key, field: Key, value: String ): AckResult
        = getAck( Cmd("HSETNX") ::: key :: field :: value :: Cmd() )

    /** Get all the values in a hash */
    def hVals ( key: Key ): BulkSeqResult
        = getBulkSeq( Cmd("HVALS") ::: key :: Cmd() )

}

