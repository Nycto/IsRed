package com.roundeights.isred

/** Parser Companion... */
object Parser {

    /** The result of parsing a chunk of bytes */
    sealed trait Result[+T] {

        /** The number of bytes consumed from the processed byte array */
        def consumed: Int

        /** Generates a new Result from this result */
        def map[A]( callback: (T) => A ): Result[A]

        /** Generates a new Result, with a modified consumed byte count */
        def addBytes( addBytes: Int ): Result[T]

        /** Executes a callback to generate a new result */
        def flatMap[A]( callback: (Int, T) => Result[A] ): Result[A]
    }

    /** Marks that a fully formed result is available */
    case class Complete[+T] (
        override val consumed: Int,
        val reply: T
    ) extends Result[T] {

        /** {@inheritDoc} */
        override def map[A]( callback: (T) => A ): Result[A]
            = new Complete( consumed, callback(reply) )

        /** {@inheritDoc} */
        override def addBytes( addBytes: Int ): Result[T]
            = new Complete( consumed + addBytes, reply )

        /** {@inheritDoc} */
        def flatMap[A]( callback: (Int, T) => Result[A] ): Result[A]
            = callback(consumed, reply)
    }

    /** Marks that more data is needed before a result is available */
    case class Incomplete[+T] (
        override val consumed: Int
    ) extends Result[T] {

        /** {@inheritDoc} */
        override def map[A]( callback: (T) => A ): Result[A]
            = new Incomplete( consumed )

        /** {@inheritDoc} */
        override def addBytes( addBytes: Int ): Result[T]
            = new Incomplete( consumed + addBytes )

        /** {@inheritDoc} */
        def flatMap[A]( callback: (Int, T) => Result[A] ): Result[A]
            = new Incomplete( consumed )
    }

}

/**
 * Parses byte arrays to produce a Response
 *
 * Parsers are mutable and single shot. You will need to instantiate a new
 * instance every time you want to parse a new response.
 */
trait Parser[+T] {

    /** Parses the given byte array */
    def parse ( bytes: Array[Byte], start: Int ): Parser.Result[T]

    /** Parses the given byte array */
    def parse ( bytes: Array[Byte] ): Parser.Result[T] = parse( bytes, 0 )

    /** Parses the given string as a UTF8 byte array */
    def parse ( str: String, start: Int ): Parser.Result[T]
        = parse( str.getBytes("UTF8"), start )

    /** Parses the given string as a UTF8 byte array */
    def parse ( str: String ): Parser.Result[T] = parse( str, 0 )

}


