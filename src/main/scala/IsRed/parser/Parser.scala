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
    def parse ( bytes: Array[Byte], start: Int ): Parser.Result[T]

    /** Parses the given byte array */
    def parse ( bytes: Array[Byte] ): Parser.Result[T] = parse( bytes, 0 )

    /** Parses the given string as a UTF8 byte array */
    def parse ( str: String, start: Int ): Parser.Result[T]
        = parse( str.getBytes("UTF8"), start )

    /** Parses the given string as a UTF8 byte array */
    def parse ( str: String ): Parser.Result[T] = parse( str, 0 )

}

/**
 * Parses byte arrays until a delimiter is reached
 */
class ParseUntil[T] (
    private val delim: Array[Byte],
    private val onComplete: (Array[Byte]) => T
) extends Parser[T] {

    /** Uses a UTF8 string as the delimiter */
    def this ( delim: String, onComplete: (Array[Byte]) => T )
        = this( delim.getBytes("UTF8"), onComplete )

    assert( delim.length > 0, "Delimieter must not be empty" )

    /** Collects the parsed data */
    private val buffer = new ByteArrayOutputStream

    /** The length of the delimiter bytes */
    private var delimLen = delim.length - 1

    /** As we encounter the delimiter, this tracks which byte to expect next */
    private var delimOffset = 0

    /** Parses the given byte array */
    override def parse ( bytes: Array[Byte], start: Int ): Parser.Result[T] = {

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

/**
 * Parses a specific number of bytes
 */
class ParseLength[T] (
    private val total: Int,
    private val onComplete: (Array[Byte]) => T
) extends Parser[T] {

    /** Collects the parsed data */
    private val buffer = new ByteArrayOutputStream

    /** The number of bytes parsed thus far */
    private var count = 0

    /** Parses the given byte array */
    override def parse ( bytes: Array[Byte], start: Int ): Parser.Result[T] = {

        val safeStart = Math.max(0, start)

        // The number of bytes still needed to fulfill this parser
        val needed = total - count

        // The number of bytes available to be read
        val available = Math.max( bytes.length - safeStart, 0 )

        // The number of bytes to actually read
        val toRead = Math.min( needed, available )

        if ( toRead > 0 ) {
            buffer.write( bytes, safeStart, toRead )
            count = count + toRead
        }

        if ( count == total )
            Parser.Complete( toRead, onComplete(buffer.toByteArray) )
        else
            Parser.Incomplete( toRead )
    }

}


