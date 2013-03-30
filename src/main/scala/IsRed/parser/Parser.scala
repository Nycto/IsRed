package com.roundeights.isred

/** Parser Companion... */
object Parser {

    /** The result of parsing a chunk of bytes */
    sealed trait Result[T] {

        /** The number of bytes consumed from the processed byte array */
        def consumed: Int
    }

    /** Marks that a fully formed result is available */
    case class Complete[T] (
        override val consumed: Int,
        val reply: T
    ) extends Result[T]

    /** Marks that more data is needed before a result is available */
    case class Incomplete[T] ( override val consumed: Int ) extends Result[T]

}

/**
 * Parses byte arrays to produce a Response
 *
 * Parsers are mutable and single shot. You will need to instantiate a new
 * instance every time you want to parse a new response.
 */
trait Parser[T] {

    /** Parses the given byte array */
    def parse ( bytes: Array[Byte] ): Parser.Result[T]

}


