package com.roundeights.isred

/**
 * Parses a Success status response
 */
class SuccessParser extends Parser[Reply] {

    /** Collects the parsed data */
    private val parser = new ParseUntil(
        Parser.ENDLINE,
        (bytes: Array[Byte]) => SuccessReply( Parser.asStr(bytes) )
    )

    /** {@inheritDoc} */
    override def parse ( bytes: Array[Byte], start: Int ): Parser.Result[Reply]
        = parser.parse( bytes, start )

}

/**
 * Parses a Failure status response
 */
class FailureParser extends Parser[Reply] {

    /** Collects the parsed data for the error code */
    private val codeParser = new ParseChain(
        new ParseUntil( Parser.SPACE, Parser.asStr(_) ),
        new ParseUntil( Parser.ENDLINE, Parser.asStr(_) )
    )

    /** {@inheritDoc} */
    override def parse (
        bytes: Array[Byte], start: Int
    ): Parser.Result[Reply] = {
        codeParser.parse(bytes, start)
            .map( result => FailureReply( result._1, result._2 ) )
    }

}

