package com.roundeights.isred

/**
 * Methods for interacting with Redis Sets
 */
trait Sets extends Iface {

    /** Add one or more members to a set */
    def sAdd ( key: Key, member: String, members: String* ): IntResult
        = getInt( Cmd("SADD") ::: key :: member :: members :: Cmd() )

    /** Get the number of members in a set */
    def sCard ( key: Key ): IntResult
        = getInt( Cmd("SCARD") ::: key :: Cmd() )

    /** Subtract multiple sets */
    def sDiff ( key: Key, keys: Key* ): BulkSetResult
        = getBulkSet( Cmd("SDIFF") ::: key :: keys :: Cmd() )

    /** Subtract multiple sets and store the resulting set in a key */
    def sDiffStore ( destination: Key, key: Key, keys: Key* ): IntResult
        = getInt( Cmd("SDIFFSTORE") ::: destination :: key :: keys :: Cmd() )

    /** Intersect multiple sets */
    def sInter ( key: Key, keys: Key* ): BulkSetResult
        = getBulkSet( Cmd("SINTER") ::: key :: keys :: Cmd() )

    /** Intersect multiple sets and store the resulting set in a key */
    def sInterStore ( destination: Key, key: Key, keys: Key* ): IntResult
        = getInt( Cmd("SINTERSTORE") ::: destination :: key :: keys :: Cmd() )

    /** Determine if a given value is a member of a set */
    def sIsMember ( key: Key, member: String ): BoolResult
        = getBool( Cmd("SISMEMBER") ::: key :: member :: Cmd() )

    /** Get all the members in a set */
    def sMembers ( key: Key ): BulkSetResult
        = getBulkSet( Cmd("SMEMBERS") ::: key :: Cmd() )

    /** Move a member from one set to another */
    def sMove ( source: Key, destination: Key, member: String ): AckResult
        = getAck( Cmd("SMOVE") ::: source :: destination :: member :: Cmd() )

    /** Remove and return a random member from a set */
    def sPop ( key: Key ): OptBulkResult
        = getOptBulk( Cmd("SPOP") ::: key :: Cmd() )

    /** Get one or multiple random members from a set */
    def sRandMember ( key: Key, count: Option[Int] = None ): BulkSetResult
        = getBulkSet( Cmd("SRANDMEMBER") ::: key :: count :: Cmd() )

    /** Get one or multiple random members from a set */
    def sRandMember ( key: Key, count: Int ): BulkSetResult
        = sRandMember( key, Some(count) )

    /** Remove one or more members from a set */
    def sRem ( key: Key, member: String, members: String ): IntResult
        = getInt( Cmd("SREM") ::: key :: member :: members :: Cmd() )

    /** Add multiple sets */
    def sUnion ( key: Key, keys: Key* ): BulkSetResult
        = getBulkSet( Cmd("SUNION") ::: key :: keys :: Cmd() )

    /** Add multiple sets and store the resulting set in a key */
    def sUnionStore ( destination: Key, key: Key, keys: Key* ): IntResult
        = getInt( Cmd("SUNIONSTORE") ::: destination :: key :: keys :: Cmd() )

}

