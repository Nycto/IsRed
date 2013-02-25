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

    /** Count set bits in a string */
    def bitCount ( key: Key, start: Option[Int], end: Option[Int] ): IntResult

    /** Count set bits in a string */
    def bitCount ( key: Key ): IntResult = bitCount( key, None, None )

    /** Count set bits in a string */
    def bitCount ( key: Key, start: Int, end: Int ): IntResult
        = bitCount( key, Some(start), Some(end) )

    /** Perform bitwise operations between strings */
    def bitOp (
        operation: BitwiseOp.Op, destkey: Key, key: Key, keys: Key*
    ): IntResult

    /** Decrement the integer value of a key by one */
    def decr ( key: Key ): IntResult

    /** Decrement the integer value of a key by the given number */
    def decrBy ( key: Key, decrement: Int ): IntResult

    /** Get the value of a key */
    def get ( key: Key ): OptBulkResult

    /** Returns the bit value at offset in the string value stored at key */
    def getBit ( key: Key, offset: Int ): IntResult

    /** Get a substring of the string stored at a key */
    def getRange ( key: Key, start: Int, end: Int ): BulkResult

    /** Set the string value of a key and return its old value */
    def getSet ( key: Key, value: String ): OptBulkResult

    /** Increment the integer value of a key by one */
    def incr ( key: Key ): IntResult

    /** Increment the integer value of a key by the given amount */
    def incrBy ( key: Key, increment: Int ): IntResult

    /** Increment the float value of a key by the given amount */
    def incrByFloat ( key: Key, increment: Int ): FloatResult

    /** Get the values of all the given keys */
    def mGet ( key: Key, keys: Key* ): BulkMapResult

    /** Set multiple keys to multiple values */
    def mSet ( pair: (Key, String), pairs: (Key, String) ): AckResult

    /** Set multiple keys to multiple values, only if none of the keys exist */
    def mSetNX ( pair: (Key, String), pairs: (Key, String) ): AckResult

    /** Set the value and expiration in milliseconds of a key */
    def pSetEx ( key: Key, milliseconds: Int, value: String ): AckResult

    /** Set the string value of a key */
    def set ( key: Key, value: String ): AckResult

    /** Sets or clears the bit at offset in the string value stored at key */
    def setBit ( key: Key, offset: Int, value: String ): IntResult

    /** Set the value and expiration of a key */
    def setEx ( key: Key, seconds: Int, value: String ): AckResult

    /** Set the value of a key, only if the key does not exist */
    def setNX ( key: Key, value: String ): AckResult

    /** Overwrite part of a string at key starting at the specified offset */
    def setRange ( key: Key, offset: Int, value: String ): IntResult

    /** Get the length of the value stored in a key */
    def strLen ( key: Key ): IntResult

}

