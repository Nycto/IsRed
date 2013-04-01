package com.roundeights.isred

/**
 * Parses an Int response
 */
class IntParser extends ParserWrap[IntReply] (
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
class StringParser extends Parser[MultiableReply] {

    /** Parses the length of the string */
    private val lengthParser = new ParseUntil(
        Parser.ENDLINE,
        (bytes: Array[Byte]) => Integer.parseInt( Parser.asStr(bytes) )
    )

    /** Parses the content of a string */
    private var contentParser: Option[Parser[MultiableReply]] = None

    /** {@inheritDoc} */
    override def parse (
        bytes: Array[Byte], start: Int
    ): Parser.Result[MultiableReply] = contentParser match {
        case None => {
            lengthParser.parse(bytes, start).flatMap( (used, length) => {
                if ( length == -1 ) {
                    Parser.Complete( used, NullReply() )
                }
                else {
                    contentParser = Some( new ParseChain(
                        new ParseLength( length, Parser.asStr _ ),
                        new ParseUntil( Parser.ENDLINE, (_: Array[Byte]) => () ),
                        (str: String, _: Unit) => StringReply(str)
                    ))

                    parse( bytes, start + used ).addBytes( used )
                }
            })
        }
        case Some(parser) => parser.parse( bytes, start )
    }
}

