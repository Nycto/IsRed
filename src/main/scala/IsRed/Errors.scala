package com.roundeights.isred

/**
 * Thrown when a Failure code is returned
 */
case class ReplyError (
    val code: String, val message: String
) extends Exception(
    "ReplyError %s: %s".format(code, message)
)

/**
 * Thrown when a command is expecting one type, but returns another
 */
case class UnexpectedReply (
    val expecting: String, val received: Reply
) extends Exception(
    "Unexpected Response Type; Expecting '%s', but received %s".format(
        expecting, received.toString
    )
)

/**
 * Thrown when a blpop or brpop timeout
 */
case class PopTimeout ( val command: Command ) extends Exception(
    "List pop timed out while requesting %s".format( command )
)

