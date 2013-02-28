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
    def bLPop ( timeout: Int, key: Key, keys: Key* ): PopResult
        = getPop( Cmd("BLPOP") ::: timeout :: key :: keys :: Cmd() )

    /**
     * Remove and get the last element in a list, or block until one
     * is available
     */
    def bRPop ( timeout: Int, key: Key, keys: Key* ): PopResult
        = getPop( Cmd("BRPOP") ::: timeout :: key :: keys :: Cmd() )

    /**
     * Pop a value from a list, push it to another list and return it; or
     * block until one is available
     */
    def bRPopLPush[A] (
        source: Key, destination: Key, timeout: Int
    ): BulkResult[A] = getBulk(
        Cmd("BRPOPLPUSH") ::: source :: destination :: timeout :: Cmd()
    )

    /** Get an element from a list by its index */
    def lIndex[A] ( key: Key, index: Int ): OptBulkResult[A]
        = getOptBulk[A]( Cmd("LINDEX") ::: key :: index :: Cmd() )

    /** Insert an element before or after another element in a list */
    def lInsert (
        key: Key, position: Position.Pos, pivot: Key, value: String
    ): IntResult = getInt(
        Cmd("LINSERT") ::: key :: position :: pivot :: value :: Cmd()
    )

    /** Get the length of a list */
    def lLen ( key: Key ): IntResult
        = getInt( Cmd("LLEN") ::: key :: Cmd() )

    /** Remove and get the first element in a list */
    def lPop[A] ( key: Key ): OptBulkResult[A]
        = getOptBulk[A]( Cmd("LPOP") ::: key :: Cmd() )

    /** Prepend one or multiple values to a list */
    def lPush ( key: Key, value: String, values: String ): IntResult
        = getInt( Cmd("LPUSH") ::: key :: value :: values :: Cmd() )

    /** Prepend a value to a list, only if the list exists */
    def lPushX ( key: Key, value: String ): IntResult
        = getInt( Cmd("LPUSHX") ::: key :: value :: Cmd() )

    /** Get a range of elements from a list */
    def lRange[A] ( key: Key, start: Int, stop: Int ): BulkSeqResult[A]
        = getBulkSeq[A]( Cmd("LRANGE") ::: key :: start :: stop :: Cmd() )

    /** Remove elements from a list */
    def lRem ( key: Key, count: Int, value: String ): IntResult
        = getInt( Cmd("LREM") ::: key :: count :: value :: Cmd() )

    /** Set the value of an element in a list by its index */
    def lSet ( key: Key, index: Int, value: String ): AckResult
        = getAck( Cmd("LSET") ::: key :: index :: value :: Cmd() )

    /** Trim a list to the specified range */
    def lTrim ( key: Key, start: Int, stop: Int ): AckResult
        = getAck( Cmd("LTRIM") ::: key :: start :: stop :: Cmd() )

    /** Remove and get the last element in a list */
    def rPop[A] ( key: Key ): OptBulkResult[A]
        = getOptBulk[A]( Cmd("RPOP") ::: key :: Cmd() )

    /**
     * Remove the last element in a list, append it to another list
     * and return it
     */
    def rPopLPush[A] ( source: Key, destination: Key ): OptBulkResult[A]
        = getOptBulk[A]( Cmd("RPOPLPUSH") ::: source :: destination :: Cmd() )

    /** Append one or multiple values to a list */
    def rPush ( key: Key, value: String, values: String* ): IntResult
        = getInt( Cmd("RPUSH") ::: key :: value :: values :: Cmd() )

    /** Append a value to a list, only if the list exists */
    def rPushX ( key: Key, value: String ): IntResult
        = getInt( Cmd("RPUSHX") ::: key :: value :: Cmd() )

}


