package com.roundeights.isred

/**
 * Methods for interacting with Redis Keys
 */
trait Keys extends Iface {

    /** Delete a key */
    def del ( key: Key, keys: Key* ): AckResult
        = getAck( Cmd("DEL") ::: key :: keys :: Cmd() )

    /** Return a serialized version of the value stored at the specified key. */
    def dump[A : Convert] ( key: Key ): BulkResult[A]
        = getBulk[A]( Cmd("DUMP") ::: key :: Cmd() )

    /** Determine if a key exists */
    def exists ( key: Key ): AckResult
        = getAck( Cmd("EXISTS") ::: key :: Cmd() )

    /** Set a key's time to live in seconds */
    def expire ( key: Key, seconds: Int ): AckResult
        = getAck( Cmd("EXPIRE") ::: key :: seconds :: Cmd() )

    /** Set the expiration for a key as a UNIX timestamp */
    def expireAt ( key: Key, timestamp: Int ): AckResult
        = getAck( Cmd("EXPIREAT") ::: key :: timestamp :: Cmd() )

    /** Find all keys matching the given pattern */
    def keys ( pattern: String ): KeyListResult
        = getKeyList( Cmd("KEYS") ::: pattern :: Cmd() )

    /** Remove the expiration from a key */
    def persist ( key: Key ): AckResult
        = getAck( Cmd("PERSIST") ::: key :: Cmd() )

    /** Set a key's time to live in milliseconds */
    def pExpire ( key: Key, milliseconds: Int ): AckResult
        = getAck( Cmd("PEXPIRE") ::: key :: milliseconds :: Cmd() )

    /**
     * Set the expiration for a key as a UNIX timestamp specified
     * in milliseconds
     */
    def pExpireAt ( key: Key, milliTimestamp: Int ): AckResult
        = getAck( Cmd("PEXPIREAT") ::: key :: milliTimestamp :: Cmd() )

    /** Get the time to live for a key in milliseconds */
    def pTTL ( key: Key ): IntResult
        = getInt( Cmd("PTTL") ::: key :: Cmd() )

    /** Return a random key from the keyspace */
    def randomKey (): KeyResult
        = getKey( Cmd("RANDOMKEY") )

    /** Rename a key */
    def rename ( key: Key, newkey: Key ): AckResult
        = getAck( Cmd("RENAME") ::: key :: newkey :: Cmd() )

    /** Rename a key, only if the new key does not exist */
    def renameNX ( key: Key, newkey: Key ): AckResult
        = getAck( Cmd("RENAMENX") ::: key :: newkey :: Cmd() )

    /**
     * Create a key using the provided serialized value, previously
     * obtained using DUMP.
     */
    def restore ( key: Key, ttl: Int, serialized: String ): AckResult
        = getAck( Cmd("RESTORE") ::: key :: ttl :: serialized :: Cmd() )

    /** Get the time to live for a key */
    def ttl ( key: Key ): IntResult
        = getInt( Cmd("TTL") ::: key :: Cmd() )

    /** Determine the type stored at key */
    def keyType ( key: Key ): KeyTypeResult
        = getKeyType( Cmd("KEYTYPE") ::: key :: Cmd() )

}

