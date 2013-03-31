package com.roundeights.isred

/**
 * Parses an Int response
 */
class IntParser extends ParserWrap[Reply] (
    new ParseUntil(
        Parser.ENDLINE,
        (bytes: Array[Byte]) => IntReply(
            Integer.parseInt( Parser.asStr(bytes) )
        )
    )
)

/**
 * Parses a String (Bulk) reply
 */
class StringParser extends Parser[Reply] {

    /** The parser for processing a String */
    private val parser = new ParseChain(
        new ParseUntil(
            Parser.ENDLINE,
            (bytes: Array[Byte]) => Integer.parseInt( Parser.asStr(bytes) )
        ),
        (length: Int) => new ParseChain(
            new ParseLength( length, Parser.asStr _ ),
            new ParseUntil( Parser.ENDLINE, (bytes: Array[Byte]) => () )
        )
    )

    /** {@inheritDoc} */
    override def parse (
        bytes: Array[Byte],
        start: Int
    ): Parser.Result[Reply] = {
        parser.parse( bytes, start ).map( tuple => StringReply( tuple._2._1 ) )
    }

}

