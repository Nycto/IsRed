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
trait Lists {

    /**
     * The result of a pop. This is generally a tuple where the first
     * element is the list that was popped from, and the second element is
     * the value that was popped.
     */
    type PopResult

    /** A bulk result */
    type BulkResult

    /** An integer result */
    type IntResult

    /** An optional bulk result */
    type OptBulkResult

    /** A sequence of bulk values */
    type BulkSeqResult

    /** A success or failures result */
    type AckResult

    /**
     * Remove and get the first element in a list, or block until one
     * is available.
     */
    def bLPop ( timeout: Int, key: Key, keys: Key* ): PopResult

    /**
     * Remove and get the last element in a list, or block until one
     * is available
     */
    def bRPop ( timeout: Int, key: Key, keys: Key* ): PopResult

    /**
     * Pop a value from a list, push it to another list and return it; or
     * block until one is available
     */
    def bRPopLPush ( source: Key, destination: Key, timeout: Int ): BulkResult

    /** Get an element from a list by its index */
    def lIndex ( key: Key, index: Int ): OptBulkResult

    /** Insert an element before or after another element in a list */
    def lInsert (
        key: Key, position: Position.Pos, pivot: Key, value: String
    ): IntResult

    /** Get the length of a list */
    def lLen ( key: Key ): IntResult

    /** Remove and get the first element in a list */
    def lPop ( key: Key ): OptBulkResult

    /** Prepend one or multiple values to a list */
    def lPush ( key: Key, value: String, values: String ): IntResult

    /** Prepend a value to a list, only if the list exists */
    def lPushX ( key: Key, value: String ): IntResult

    /** Get a range of elements from a list */
    def lRange ( key: Key, start: Int, stop: Int ): BulkSeqResult

    /** Remove elements from a list */
    def lRem ( key: Key, count: Int, value: String ): IntResult

    /** Set the value of an element in a list by its index */
    def lSet ( key: Key, index: Int, value: String ): AckResult

    /** Trim a list to the specified range */
    def lTrim ( key: Key, start: Int, stop: Int ): AckResult

    /** Remove and get the last element in a list */
    def rPop ( key: Key ): OptBulkResult

    /**
     * Remove the last element in a list, append it to another list
     * and return it
     */
    def rPopLPush ( source: Key, destination: Key ): OptBulkResult

    /** Append one or multiple values to a list */
    def rPush ( key: Key, value: String, values: String* ): IntResult

    /** Append a value to a list, only if the list exists */
    def rPushX ( key: Key, value: String ): IntResult

}


