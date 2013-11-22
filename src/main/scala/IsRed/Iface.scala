package com.roundeights.isred

import scala.language.higherKinds

/**
 * An interface for constructing Redis commands
 */
trait Iface {

    /** The type definition for implicit result generation */
    type Convert[A] = (Reply) => A


    /** A generic reply that could be any type */
    type AnyResult[A]


    /** A success or failure result */
    type AckResult


    /** An integer result */
    type IntResult

    /** A floating point number result */
    type FloatResult

    /** A Boolean result */
    type BoolResult


    /** A string, int or float result */
    type BulkResult[A]

    /** An optional bulk result */
    type OptBulkResult[A]

    /** A set of bulk values */
    type BulkSetResult[A]

    /** A sequence of bulk values */
    type BulkSeqResult[A]

    /** A map of bulk values */
    type BulkMapResult[A]


    /** The result of a TTL inquiry */
    type TtlResult

    /**
     * The result of a pop. This is generally a tuple where the first
     * element is the list that was popped from, and the second element is
     * the value that was popped.
     */
    type PopResult[A]

    /** A list of keys */
    type KeyListResult

    /** A single key result */
    type KeyResult

    /** A result describing the type of a key */
    type KeyTypeResult


    /** A result of any type */
    private[isred] def getAny[A: Convert]( command: Command ): AnyResult[A]


    /** A success or failure result */
    private[isred] def getAck( command: Command ): AckResult


    /** An integer result */
    private[isred] def getInt( command: Command ): IntResult

    /** A floating point number result */
    private[isred] def getFloat( command: Command ): FloatResult

    /** A Boolean result */
    private[isred] def getBool( command: Command ): BoolResult


    /** A string, int or float result */
    private[isred] def getBulk[A: Convert]( command: Command ): BulkResult[A]

    /** An optional bulk result */
    private[isred] def getOptBulk[A: Convert](
        command: Command
    ): OptBulkResult[A]

    /** A set of bulk values */
    private[isred] def getBulkSet[A: Convert](
        command: Command
    ): BulkSetResult[A]

    /** A sequence of bulk values */
    private[isred] def getBulkSeq[A: Convert](
        command: Command
    ): BulkSeqResult[A]

    /** A map of bulk values */
    private[isred] def getBulkMap[A: Convert](
        command: Command
    ): BulkMapResult[A]


    /**
     * The result of a ttl query
     */
    private[isred] def getTtl( command: Command ): TtlResult

    /**
     * The result of a pop. This is generally a tuple where the first
     * element is the list that was popped from, and the second element is
     * the value that was popped.
     */
    private[isred] def getPop[A : Convert]( command: Command ): PopResult[A]

    /** A list of keys */
    private[isred] def getKeyList( command: Command ): KeyListResult

    /** A single key result */
    private[isred] def getKey( command: Command ): KeyResult

    /** A result describing the type of a key */
    private[isred] def getKeyType( command: Command ): KeyTypeResult

}

