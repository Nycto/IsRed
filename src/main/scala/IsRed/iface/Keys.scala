package com.roundeights.isred

/**
 * The different types of keys
 */
object KeyType extends Enumeration {
    type Type = Value
    val STRING = Value("string")
    val LIST = Value("list")
    val SET = Value("set")
    val ZSET = Value("zset")
    val HASH = Value("hash")
    val NONE = Value("none")

    /** Returns a key type from a string */
    def fromString (name: String): KeyType.Type = {
        values.filter(_.toString == name).headOption.getOrElse(
            throw new IllegalArgumentException(
                "Invalid key type: %s".format( name )
            )
        )
    }
}

/**
 * Methods for interacting with Redis Keys
 */
trait Keys extends Iface {

    /** Delete a key */
    def del ( key: Key, keys: Key* ): BoolResult
        = getBool( "DEL" ::: key :: keys :: Cmd() )

    /** Return a serialized version of the value stored at the specified key. */
    def dump ( key: Key ): BulkResult[Array[Byte]]
        = getBulk[Array[Byte]]( "DUMP" ::: key :: Cmd() )

    /** Determine if a key exists */
    def exists ( key: Key ): BoolResult = getBool( "EXISTS" ::: key :: Cmd() )

    /** Set a key's time to live in seconds */
    def expire ( key: Key, seconds: Int ): AckResult
        = getAck( "EXPIRE" ::: key :: seconds :: Cmd() )

    /** Set the expiration for a key as a UNIX timestamp */
    def expireAt ( key: Key, timestamp: Int ): AckResult
        = getAck( "EXPIREAT" ::: key :: timestamp :: Cmd() )

    /** Find all keys matching the given pattern */
    def keys ( pattern: String ): KeyListResult
        = getKeyList( "KEYS" ::: pattern :: Cmd() )

    /** Remove the expiration from a key */
    def persist ( key: Key ): AckResult = getAck( "PERSIST" ::: key :: Cmd() )

    /** Set a key's time to live in milliseconds */
    def pExpire ( key: Key, milliseconds: Int ): AckResult
        = getAck( "PEXPIRE" ::: key :: milliseconds :: Cmd() )

    /**
     * Set the expiration for a key as a UNIX timestamp specified
     * in milliseconds
     */
    def pExpireAt ( key: Key, milliTimestamp: Int ): AckResult
        = getAck( "PEXPIREAT" ::: key :: milliTimestamp :: Cmd() )

    /** Get the time to live for a key in milliseconds */
    def pTtl ( key: Key ): TtlResult = getTtl( "PTTL" ::: key :: Cmd() )

    /** Return a random key from the keyspace */
    def randomKey (): KeyResult = getKey( Cmd("RANDOMKEY") )

    /** Rename a key */
    def rename ( key: Key, newkey: Key ): AckResult
        = getAck( "RENAME" ::: key :: newkey :: Cmd() )

    /** Rename a key, only if the new key does not exist */
    def renameNX ( key: Key, newkey: Key ): AckResult
        = getAck( "RENAMENX" ::: key :: newkey :: Cmd() )

    /**
     * Create a key using the provided serialized value, previously
     * obtained using DUMP.
     */
    def restore ( key: Key, ttl: Int, serialized: Array[Byte] ): AckResult
        = getAck( "RESTORE" ::: key :: ttl :: serialized :: Cmd() )

    /**
     * Create a key using the provided serialized value, previously
     * obtained using DUMP.
     */
    def restore ( key: Key, serialized: Array[Byte] ): AckResult
        = restore( key, 0, serialized )

    /** Get the time to live for a key */
    def ttl ( key: Key ): TtlResult = getTtl( "TTL" ::: key :: Cmd() )

    /** Determine the type stored at key */
    def keyType ( key: Key ): KeyTypeResult
        = getKeyType( "TYPE" ::: key :: Cmd() )

}

