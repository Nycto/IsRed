package com.roundeights.isred

import java.io.ByteArrayOutputStream
import scala.annotation.tailrec

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
    def parse ( bytes: Array[Byte], start: Int = 0 ): Parser.Result[T]

}

/**
 * Parses byte arrays until a delimiter is reached
 */
class ParseUntil[T] (
    private val delim: Array[Byte],
    private val onComplete: (Array[Byte]) => T
) extends Parser[T] {

    assert( delim.length > 0, "Delimieter must not be empty" )

    /** Collects the parsed data */
    private val buffer = new ByteArrayOutputStream

    /** The length of the delimiter bytes */
    private var delimLen = delim.length - 1

    /** As we encounter the delimiter, this tracks which byte to expect next */
    private var delimOffset = 0

    /** Parses the given byte array */
    override def parse (
        bytes: Array[Byte],
        start: Int = 0
    ): Parser.Result[T] = {

        // Recursively read bytes from the input array
        @tailrec def read ( offset: Int ): Parser.Result[T] = {
            if ( offset >= bytes.length ) {
                Parser.Incomplete( offset - start )
            }
            else if (
                bytes(offset) == delim(delimOffset)
                && delimOffset == delimLen
            ) {
                Parser.Complete(
                    offset + 1 - start,
                    onComplete(buffer.toByteArray)
                )
            }
            else {
                if ( bytes(offset) == delim(delimOffset) ) {
                    delimOffset = delimOffset + 1
                }
                else if ( delimOffset != 0 ) {
                    buffer.write( delim, 0, delimOffset )
                    buffer.write( bytes(offset) )
                    delimOffset = 0
                }
                else {
                    buffer.write( bytes(offset) )
                }

                read( offset + 1 )
            }
        }

        read( start )
    }

}


