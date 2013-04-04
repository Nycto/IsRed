package com.roundeights.isred

import scala.language.higherKinds

/**
 * An interface for constructing Redis commands
 */
trait Iface {

    /** The type definition for implicit result generation */
    type Convert[A] = (Reply) => A


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


    /** A success or failure result */
    def getAck( command: Command ): AckResult


    /** An integer result */
    def getInt( command: Command ): IntResult

    /** A floating point number result */
    def getFloat( command: Command ): FloatResult

    /** A Boolean result */
    def getBool( command: Command ): BoolResult


    /** A string, int or float result */
    def getBulk[A: Convert]( command: Command ): BulkResult[A]

    /** An optional bulk result */
    def getOptBulk[A: Convert]( command: Command ): OptBulkResult[A]

    /** A set of bulk values */
    def getBulkSet[A: Convert]( command: Command ): BulkSetResult[A]

    /** A sequence of bulk values */
    def getBulkSeq[A: Convert]( command: Command ): BulkSeqResult[A]

    /** A map of bulk values */
    def getBulkMap[A: Convert]( command: Command ): BulkMapResult[A]


    /**
     * The result of a pop. This is generally a tuple where the first
     * element is the list that was popped from, and the second element is
     * the value that was popped.
     */
    def getPop( command: Command ): PopResult

    /** A list of keys */
    def getKeyList( command: Command ): KeyListResult

    /** A single key result */
    def getKey( command: Command ): KeyResult

    /** A result describing the type of a key */
    def getKeyType( command: Command ): KeyTypeResult

}

