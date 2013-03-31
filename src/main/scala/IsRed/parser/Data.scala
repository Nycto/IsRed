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
class StringParser extends ParserWrap[Reply](
    new ParseChain(
        new ParseUntil(
            Parser.ENDLINE,
            (bytes: Array[Byte]) => Integer.parseInt( Parser.asStr(bytes) )
        ),
        (length: Int) => new ParseChain(
            new ParseLength( length, Parser.asStr _ ),
            new ParseUntil( Parser.ENDLINE, (bytes: Array[Byte]) => () ),
            (str: String, _: Unit) => StringReply(str)
        ),
        (_: Int, output: Reply) => output
    )
)

