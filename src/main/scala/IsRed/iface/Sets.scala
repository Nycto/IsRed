package com.roundeights.isred

/**
 * Methods for interacting with Redis Sets
 */
trait Sets extends Iface {

    /** Add one or more members to a set */
    def sAdd ( key: Key, member: String, members: String* ): IntResult

    /** Get the number of members in a set */
    def sCard ( key: Key ): IntResult

    /** Subtract multiple sets */
    def sDiff ( key: Key, keys: Key* ): BulkSetResult

    /** Subtract multiple sets and store the resulting set in a key */
    def sDiffStore ( destination: Key, key: Key, keys: Key* ): IntResult

    /** Intersect multiple sets */
    def sInter ( key: Key, keys: Key* ): BulkSetResult

    /** Intersect multiple sets and store the resulting set in a key */
    def sInterStore ( destination: Key, key: Key, keys: Key* ): IntResult

    /** Determine if a given value is a member of a set */
    def sIsMember ( key: Key, member: String ): BoolResult

    /** Get all the members in a set */
    def sMembers ( key: Key ): BulkSetResult

    /** Move a member from one set to another */
    def sMove ( source: Key, destination: Key, member: String ): AckResult

    /** Remove and return a random member from a set */
    def sPop ( key: Key ): OptBulkResult

    /** Get one or multiple random members from a set */
    def sRandMember ( key: Key, count: Option[Int] = None ): BulkSetResult

    /** Get one or multiple random members from a set */
    def sRandMember ( key: Key, count: Int ): BulkSetResult
        = sRandMember( key, Some(count) )

    /** Remove one or more members from a set */
    def sRem ( key: Key, member: String, members: String ): IntResult

    /** Add multiple sets */
    def sUnion ( key: Key, keys: Key* ): BulkSetResult

    /** Add multiple sets and store the resulting set in a key */
    def sUnionStore ( destination: Key, key: Key, keys: Key* ): IntResult

}

