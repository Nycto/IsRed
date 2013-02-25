package com.roundeights.isred

/**
 * Methods for interacting with Redis Hashes
 */
trait Hashes extends Iface {

    /** Delete one or more hash fields */
    def hDel ( key: Key, field: Key, fields: Key* ): IntResult

    /** Determine if a hash field exists */
    def hExists ( key: Key, field: Key ): BoolResult

    /** Get the value of a hash field */
    def hGet ( key: Key, field: Key ): OptBulkResult

    /** Get all the fields and values in a hash */
    def hGetAll ( key: Key ): BulkMapResult

    /** Increment the integer value of a hash field by the given number */
    def hIncrBy ( key: Key, field: Key, increment: Int ): IntResult

    /** Increment the float value of a hash field by the given amount */
    def hIncrByFloat ( key: Key, field: Key, increment: Float ): FloatResult

    /** Get all the fields in a hash */
    def hKeys ( key: Key ): BulkSetResult

    /** Get the number of fields in a hash */
    def hLen ( key: Key ): IntResult

    /** Get the values of all the given hash fields */
    def hMGet ( key: Key, field: Key, fields: Key ): BulkMapResult

    /** Set multiple hash fields to multiple values */
    def hMSet (
        key: Key, pair: (Key, String), pairs: (Key, String)*
    ): AckResult

    /** Set the string value of a hash field */
    def hSet ( key: Key, field: Key, value: String ): AckResult

    /** Set the value of a hash field, only if the field does not exist */
    def hSetNX ( key: Key, field: Key, value: String ): AckResult

    /** Get all the values in a hash */
    def hVals ( key: Key ): BulkSeqResult

}

