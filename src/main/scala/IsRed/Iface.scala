package com.roundeights.isred

/**
 * An interface for constructing Redis commands
 */
trait Iface {

    /** A success or failure result */
    type AckResult


    /** A string, int or float result */
    type BulkResult

    /** An integer result */
    type IntResult

    /** A floating point number result */
    type FloatResult

    /** A Boolean result */
    type BoolResult


    /** An optional bulk result */
    type OptBulkResult

    /** A set of bulk values */
    type BulkSetResult

    /** A sequence of bulk values */
    type BulkSeqResult

    /** A map of bulk values */
    type BulkMapResult


    /**
     * The result of a pop. This is generally a tuple where the first
     * element is the list that was popped from, and the second element is
     * the value that was popped.
     */
    type PopResult

    /** A list of keys */
    type KeyListResult

    /** A single key result */
    type KeyResult

    /** A result describing the type of a key */
    type KeyTypeResult

}

