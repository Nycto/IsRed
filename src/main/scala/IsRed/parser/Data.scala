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

