package com.roundeights.isred

import java.io.ByteArrayOutputStream
import scala.annotation.tailrec


/**
 * Parses byte arrays until a delimiter is reached
 */
class ParseUntil[+T] (
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
class ParseLength[+T] (
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

/**
 * Parses with one parser until it completes, then moves on to another parser.
 */
class ParseChain[A,B,R] (
    private val first: Parser[A],
    private val second: (A) => Parser[B],
    private val finish: (A,B) => R
) extends Parser[R] {

    /** The completed value from the first parser */
    private var firstResult: Option[A] = None

    /** Becomes populated with the second parser */
    private var secondParser: Option[Parser[B]] = None

    /** An alternate constructor, for when a deferred build isn't needed */
    def this ( first: Parser[A], second: Parser[B], finish: (A,B) => R )
        = this( first, (_) => second, finish )

    /** {@inheritDoc} */
    override def parse (
        bytes: Array[Byte], start: Int
    ): Parser.Result[R] = firstResult match {
        case None => {
            first.parse( bytes, start ).flatMap( (used, result) => {
                firstResult = Some(result)

                if ( start + used >= bytes.length )
                    Parser.Incomplete(used)
                else
                    parse( bytes, start + used ).addBytes( used )
            })
        }
        case Some(_) => {
            if ( secondParser.isEmpty )
                secondParser = Some( second(firstResult.get) )

            secondParser.get
                .parse( bytes, start )
                .map( secondResult => finish(firstResult.get, secondResult) )
        }
    }

}

/**
 * A parser that wraps another parser
 */
class ParserWrap[T] ( private val parser: Parser[T] ) extends Parser[T] {

    /** {@inheritDoc} */
    override def parse ( bytes: Array[Byte], start: Int ): Parser.Result[T]
        = parser.parse( bytes, start )
}


