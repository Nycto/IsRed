package com.roundeights.isred

/**
 * Parses a Success status response
 */
class SuccessParser extends ParserWrap[SuccessReply] (
    new ParseUntil(
        Parser.ENDLINE,
        (bytes: Array[Byte]) => SuccessReply( Parser.asStr(bytes) )
    )
)

/**
 * Parses a Failure status response
 */
class FailureParser extends ParserWrap[FailureReply] (
    new ParseChain(
        new ParseUntil( Parser.SPACE, Parser.asStr(_) ),
        new ParseUntil( Parser.ENDLINE, Parser.asStr(_) ),
        (code: String, msg: String) => FailureReply(code, msg)
    )
)


