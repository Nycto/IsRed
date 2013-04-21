package com.roundeights.isred

/** Thrown when an unexpected byte is encountered */
case class UnexpectedByte(
    byte: Byte, allowed: Iterable[Byte]
) extends Exception(
    "Unexpected byte encountered: %d; Allowed bytes: %s".format(
        byte.toInt,
        allowed.map( _.toInt ).mkString(", ")
    )
)

/** Parser Companion... */
private[isred] object Parser {


    /** The result of parsing a chunk of bytes */
    private[isred] sealed trait Result[+T] {

        /** Returns the value, if it exists, or throws */
        def get: T

        /** The number of bytes consumed from the processed byte array */
        def consumed: Int

        /** Executes a callback if this parser is Complete */
        def foreach[A]( callback: (T) => Unit ): Unit

        /** Generates a new Result from this result */
        def map[A]( callback: (T) => A ): Result[A]

        /** Generates a new Result, with a modified consumed byte count */
        def addBytes( addBytes: Int ): Result[T]

        /** Executes a callback to generate a new result */
        def flatMap[A]( callback: (Int, T) => Result[A] ): Result[A]
    }

    /** Marks that a fully formed result is available */
    private[isred] case class Complete[+T] (
        override val consumed: Int,
        override val get: T
    ) extends Result[T] {

        /** {@inheritDoc} */
        override def foreach[A]( callback: (T) => Unit ): Unit = callback(get)

        /** {@inheritDoc} */
        override def map[A]( callback: (T) => A ): Result[A]
            = new Complete( consumed, callback(get) )

        /** {@inheritDoc} */
        override def addBytes( addBytes: Int ): Result[T]
            = new Complete( consumed + addBytes, get )

        /** {@inheritDoc} */
        def flatMap[A]( callback: (Int, T) => Result[A] ): Result[A]
            = callback( consumed, get )
    }

    /** Marks that more data is needed before a result is available */
    private[isred] case class Incomplete[+T] (
        override val consumed: Int
    ) extends Result[T] {

        /** {@inheritDoc} */
        override def get: T = throw new NoSuchElementException

        /** {@inheritDoc} */
        override def foreach[A]( callback: (T) => Unit ): Unit = ()

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


    /** Converts a byte array to a UTF8 string */
    private [isred] def asStr ( bytes: Array[Byte] )
        = new String(bytes, "UTF8")

    /** A pass through method */
    private [isred] def noop ( bytes: Array[Byte] ) = bytes

    /** A data sink */
    private [isred] def void ( bytes: Array[Byte] ): Unit = ()


    /** A shared \r\n byte array */
    private[isred] val ENDLINE: Array[Byte] = "\r\n".getBytes("UTF8")

    /** A shared " " byte array */
    private[isred] val SPACE: Array[Byte] = " ".getBytes("UTF8")


    /**
     * Generates a parser for reading a full Redis reply
     */
    def apply(): Parser[Reply] = new ParseSwitch[Reply](
        '+' -> new SuccessParser,
        '-' -> new FailureParser,
        '*' -> new MultiParser,
        ':' -> new IntParser,
        '$' -> new StringParser
    )

    /** Generates a human readable version of a byte array */
    def readable ( bytes: Array[Byte] ) = {
        bytes.map( (byte: Byte) => {
            if ( byte == 9 ) "\\t"
            else if ( byte == 10 ) "\\n"
            else if ( byte == 13 ) "\\r"
            else if ( byte < 33 || byte > 126 ) "\\x%02x".format(byte)
            else "%c".format(byte)
        }).mkString
    }
}

/**
 * Parses byte arrays to produce a Response
 *
 * Parsers are mutable and single shot. You will need to instantiate a new
 * instance every time you want to parse a new response.
 */
private[isred] trait Parser[+T] {

    /** Parses the given byte array */
    def parse ( bytes: Array[Byte], start: Int ): Parser.Result[T]

    /** Parses a list of ints as bytes */
    def parse ( bytes: Int* ): Parser.Result[T]
        = parse( bytes.map( _.toByte ).toArray, 0 )

    /** Parses the given byte array */
    def parse ( bytes: Array[Byte] ): Parser.Result[T] = parse( bytes, 0 )

    /** Parses the given string as a UTF8 byte array */
    def parse ( str: String, start: Int ): Parser.Result[T]
        = parse( str.getBytes("UTF8"), start )

    /** Parses the given string as a UTF8 byte array */
    def parse ( str: String ): Parser.Result[T] = parse( str, 0 )

}


