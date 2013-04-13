package com.roundeights.isred

/**
 * Supported bitwise operations
 */
object BitwiseOp extends Enumeration {
    type Op = Value
    val AND = Value("AND")
    val OR  = Value("OR")
    val XOR = Value("XOR")
    val NOT = Value("NOT")
}

/**
 * Methods for interacting with Redis Strings
 */
trait Strings extends Iface {

    /** Append a value to a key */
    def append ( key: Key, value: String ): IntResult
        = getInt( Cmd("APPEND") ::: key :: value :: Cmd() )

    /** Count set bits in a string */
    def bitCount ( key: Key, start: Option[Int], end: Option[Int] ): IntResult
        = getInt( Cmd("BITCOUNT") ::: key :: start :: end :: Cmd() )

    /** Count set bits in a string */
    def bitCount ( key: Key ): IntResult
        = bitCount( key, None, None )

    /** Count set bits in a string */
    def bitCount ( key: Key, start: Int, end: Int ): IntResult
        = bitCount( key, Some(start), Some(end) )

    /** Perform bitwise operations between strings */
    def bitOp (
        operation: BitwiseOp.Op, destkey: Key, key: Key, keys: Key*
    ): IntResult = getInt(
        Cmd("BITOP") ::: operation :: destkey :: key :: keys :: Cmd()
    )

    /** Decrement the integer value of a key by one */
    def decr ( key: Key ): IntResult
        = getInt( Cmd("DECR") ::: key :: Cmd() )

    /** Decrement the integer value of a key by the given number */
    def decrBy ( key: Key, decrement: Int ): IntResult
        = getInt( Cmd("DECRBY") ::: key :: decrement :: Cmd() )

    /** Get the value of a key */
    def get[A : Convert] ( key: Key ): OptBulkResult[A]
        = getOptBulk[A]( Cmd("GET") ::: key :: Cmd() )

    /** Returns the bit value at offset in the string value stored at key */
    def getBit ( key: Key, offset: Int ): IntResult
        = getInt( Cmd("GETBIT") ::: key :: offset :: Cmd() )

    /** Get a substring of the string stored at a key */
    def getRange[A : Convert] ( key: Key, start: Int, end: Int ): BulkResult[A]
        = getBulk[A]( Cmd("GETRANGE") ::: key :: start :: end :: Cmd() )

    /** Set the string value of a key and return its old value */
    def getSet[A : Convert] ( key: Key, value: String ): OptBulkResult[A]
        = getOptBulk[A]( Cmd("GETSET") ::: key :: value :: Cmd() )

    /** Increment the integer value of a key by one */
    def incr ( key: Key ): IntResult
        = getInt( Cmd("INCR") ::: key :: Cmd() )

    /** Increment the integer value of a key by the given amount */
    def incrBy ( key: Key, increment: Int ): IntResult
        = getInt( Cmd("INCRBY") ::: key :: increment :: Cmd() )

    /** Increment the float value of a key by the given amount */
    def incrByFloat ( key: Key, increment: Int ): FloatResult
        = getFloat( Cmd("INCRBYFLOAT") ::: key :: increment :: Cmd() )

    /** Get the values of all the given keys */
    def mGet[A : Convert] ( key: Key, keys: Key* ): BulkMapResult[A]
        = getBulkMap[A]( Cmd("MGET") ::: key :: keys :: Cmd() )

    /** Set multiple keys to multiple values */
    def mSet ( pair: (Key, String), pairs: (Key, String)* ): AckResult
        = getAck( Cmd("MSET") ::: pair :: pairs :: Cmd() )

    /** Set multiple keys to multiple values, only if none of the keys exist */
    def mSetNX ( pair: (Key, String), pairs: (Key, String)* ): AckResult
        = getAck( Cmd("MSETNX") ::: pair :: pairs :: Cmd() )

    /** Set the value and expiration in milliseconds of a key */
    def pSetEx ( key: Key, milliseconds: Int, value: String ): AckResult
        = getAck( Cmd("PSETEX") ::: key :: milliseconds :: value :: Cmd() )

    /** Set the string value of a key */
    def set ( key: Key, value: String ): AckResult
        = getAck( Cmd("SET") ::: key :: value :: Cmd() )

    /** Sets or clears the bit at offset in the string value stored at key */
    def setBit ( key: Key, offset: Int, value: String ): IntResult
        = getInt( Cmd("SETBIT") ::: key :: offset :: value :: Cmd() )

    /** Set the value and expiration of a key */
    def setEx ( key: Key, seconds: Int, value: String ): AckResult
        = getAck( Cmd("SETEX") ::: key :: seconds :: value :: Cmd() )

    /** Set the value of a key, only if the key does not exist */
    def setNX ( key: Key, value: String ): AckResult
        = getAck( Cmd("SETNX") ::: key :: value :: Cmd() )

    /** Overwrite part of a string at key starting at the specified offset */
    def setRange ( key: Key, offset: Int, value: String ): IntResult
        = getInt( Cmd("SETRANGE") ::: key :: offset :: value :: Cmd() )

    /** Get the length of the value stored in a key */
    def strLen ( key: Key ): IntResult
        = getInt( Cmd("STRLEN") ::: key :: Cmd() )

}

