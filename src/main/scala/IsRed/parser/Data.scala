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

/**
 * Parses a multi-bulk reply
 */
class MultiParser extends Parser[Reply] {

    import scala.collection.mutable.Buffer

    /** Parses the number of args to expect */
    private val argsParser = new ParseUntil(
        Parser.ENDLINE,
        (bytes: Array[Byte]) => Integer.parseInt( Parser.asStr(bytes) )
    )

    /** The number of args */
    private var argsOpt: Option[Int] = None

    /** The list of replies */
    private val replies = Buffer[MultiableReply]()

    /** Parses an individual reply */
    private var replyParser: Parser[MultiableReply] = null

    /** Builds a new Multiable parser */
    private def buildReplyParser: Parser[MultiableReply] = new ParseSwitch(
        ':' -> new IntParser,
        '$' -> new StringParser
    )

    /** {@inheritDoc} */
    override def parse (
        bytes: Array[Byte], start: Int
    ): Parser.Result[Reply] = argsOpt match {
        case None => {
            argsParser.parse( bytes, start ).flatMap( (used, args) => {
                argsOpt = Some( args )
                replyParser = buildReplyParser
                parse( bytes, start + used ).addBytes( used )
            })
        }
        case Some(args) if args < 0 => {
            Parser.Complete( 0, NullReply() )
        }
        case Some(args) if replies.length < args => {
            replyParser.parse( bytes, start ).flatMap( (used, reply) => {
                replies += reply
                replyParser = buildReplyParser
                parse( bytes, start + used ).addBytes( used )
            })
        }
        case Some(args) => {
            Parser.Complete( 0, new MultiReply(replies.toList:_*) )
        }
    }

}

