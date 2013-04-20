package com.roundeights.isred

/**
 * Positional marker
 */
object Position extends Enumeration {
    type Pos = Value
    val BEFORE = Value("BEFORE")
    val AFTER = Value("AFTER")
}

/**
 * Methods for interacting with Redis Lists
 */
trait Lists extends Iface {

    /**
     * Remove and get the first element in a list, or block until one
     * is available.
     */
    def bLPop[A : Convert] ( timeout: Int, key: Key, keys: Key* ): PopResult[A]
        = getPop[A]( "BLPOP" ::: timeout :: key :: keys :: Cmd() )

    /**
     * Remove and get the last element in a list, or block until one
     * is available
     */
    def bRPop[A : Convert] ( timeout: Int, key: Key, keys: Key* ): PopResult[A]
        = getPop[A]( "BRPOP" ::: timeout :: key :: keys :: Cmd() )

    /**
     * Pop a value from a list, push it to another list and return it; or
     * block until one is available
     */
    def bRPopLPush[A : Convert] (
        source: Key, destination: Key, timeout: Int
    ): BulkResult[A] = getBulk(
        "BRPOPLPUSH" ::: source :: destination :: timeout :: Cmd()
    )

    /** Get an element from a list by its index */
    def lIndex[A : Convert] ( key: Key, index: Int ): OptBulkResult[A]
        = getOptBulk[A]( "LINDEX" ::: key :: index :: Cmd() )

    /** Insert an element before or after another element in a list */
    def lInsert (
        key: Key, position: Position.Pos, pivot: Key, value: String
    ): IntResult = getInt(
        "LINSERT" ::: key :: position :: pivot :: value :: Cmd()
    )

    /** Get the length of a list */
    def lLen ( key: Key ): IntResult = getInt( "LLEN" ::: key :: Cmd() )

    /** Remove and get the first element in a list */
    def lPop[A : Convert] ( key: Key ): OptBulkResult[A]
        = getOptBulk[A]( "LPOP" ::: key :: Cmd() )

    /** Prepend one or multiple values to a list */
    def lPush ( key: Key, value: String, values: String ): IntResult
        = getInt( "LPUSH" ::: key :: value :: values :: Cmd() )

    /** Prepend a value to a list, only if the list exists */
    def lPushX ( key: Key, value: String ): IntResult
        = getInt( "LPUSHX" ::: key :: value :: Cmd() )

    /** Get a range of elements from a list */
    def lRange[A : Convert] (
        key: Key, start: Int, stop: Int
    ): BulkSeqResult[A] = {
        getBulkSeq[A]( "LRANGE" ::: key :: start :: stop :: Cmd() )
    }

    /** Remove elements from a list */
    def lRem ( key: Key, count: Int, value: String ): IntResult
        = getInt( "LREM" ::: key :: count :: value :: Cmd() )

    /** Set the value of an element in a list by its index */
    def lSet ( key: Key, index: Int, value: String ): AckResult
        = getAck( "LSET" ::: key :: index :: value :: Cmd() )

    /** Trim a list to the specified range */
    def lTrim ( key: Key, start: Int, stop: Int ): AckResult
        = getAck( "LTRIM" ::: key :: start :: stop :: Cmd() )

    /** Remove and get the last element in a list */
    def rPop[A : Convert] ( key: Key ): OptBulkResult[A]
        = getOptBulk[A]( "RPOP" ::: key :: Cmd() )

    /**
     * Remove the last element in a list, append it to another list
     * and return it
     */
    def rPopLPush[A : Convert] (
        source: Key, destination: Key
    ): OptBulkResult[A] = {
        getOptBulk[A]( "RPOPLPUSH" ::: source :: destination :: Cmd() )
    }

    /** Append one or multiple values to a list */
    def rPush ( key: Key, value: String, values: String* ): IntResult
        = getInt( "RPUSH" ::: key :: value :: values :: Cmd() )

    /** Append a value to a list, only if the list exists */
    def rPushX ( key: Key, value: String ): IntResult
        = getInt( "RPUSHX" ::: key :: value :: Cmd() )

}


