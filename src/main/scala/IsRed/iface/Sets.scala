package com.roundeights.isred

/**
 * Methods for interacting with Redis Sets
 */
trait Sets extends Iface {

    /** Add one or more members to a set */
    def sAdd ( key: Key, member: String, members: String* ): IntResult
        = getInt( "SADD" ::: key :: member :: members :: Cmd() )

    /** Get the number of members in a set */
    def sCard ( key: Key ): IntResult = getInt( "SCARD" ::: key :: Cmd() )

    /** Subtract multiple sets */
    def sDiff[A : Convert] ( key: Key, keys: Key* ): BulkSetResult[A]
        = getBulkSet[A]( "SDIFF" ::: key :: keys :: Cmd() )

    /** Subtract multiple sets and store the resulting set in a key */
    def sDiffStore ( destination: Key, key: Key, keys: Key* ): IntResult
        = getInt( "SDIFFSTORE" ::: destination :: key :: keys :: Cmd() )

    /** Intersect multiple sets */
    def sInter[A : Convert] ( key: Key, keys: Key* ): BulkSetResult[A]
        = getBulkSet[A]( "SINTER" ::: key :: keys :: Cmd() )

    /** Intersect multiple sets and store the resulting set in a key */
    def sInterStore ( destination: Key, key: Key, keys: Key* ): IntResult
        = getInt( "SINTERSTORE" ::: destination :: key :: keys :: Cmd() )

    /** Determine if a given value is a member of a set */
    def sIsMember ( key: Key, member: String ): BoolResult
        = getBool( "SISMEMBER" ::: key :: member :: Cmd() )

    /** Get all the members in a set */
    def sMembers[A : Convert] ( key: Key ): BulkSetResult[A]
        = getBulkSet[A]( "SMEMBERS" ::: key :: Cmd() )

    /** Move a member from one set to another */
    def sMove ( source: Key, destination: Key, member: String ): AckResult
        = getAck( "SMOVE" ::: source :: destination :: member :: Cmd() )

    /** Remove and return a random member from a set */
    def sPop[A : Convert] ( key: Key ): OptBulkResult[A]
        = getOptBulk[A]( "SPOP" ::: key :: Cmd() )

    /** Get one or multiple random members from a set */
    def sRandMember[A : Convert] (
        key: Key, count: Option[Int] = None
    ): BulkSetResult[A] = {
        getBulkSet[A]( "SRANDMEMBER" ::: key :: count :: Cmd() )
    }

    /** Get one or multiple random members from a set */
    def sRandMember[A : Convert] ( key: Key, count: Int ): BulkSetResult[A]
        = sRandMember( key, Some(count) )

    /** Remove one or more members from a set */
    def sRem ( key: Key, member: String, members: String ): IntResult
        = getInt( "SREM" ::: key :: member :: members :: Cmd() )

    /** Add multiple sets */
    def sUnion[A : Convert] ( key: Key, keys: Key* ): BulkSetResult[A]
        = getBulkSet[A]( "SUNION" ::: key :: keys :: Cmd() )

    /** Add multiple sets and store the resulting set in a key */
    def sUnionStore ( destination: Key, key: Key, keys: Key* ): IntResult
        = getInt( "SUNIONSTORE" ::: destination :: key :: keys :: Cmd() )

}

