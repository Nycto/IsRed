package com.roundeights.isred

/**
 * A Raw response from Redis
 */
sealed trait Reply

/**
 * A Success Code response
 */
case class SuccessReply ( val code: String ) extends Reply

/**
 * A Failed response
 */
case class FailureReply ( val code: String, val message: String ) extends Reply

/**
 * Multi-able responses can be returned as part of a MultiReply
 */
sealed trait MultiableReply extends Reply

/**
 * A aggregating response for returning multiple responses
 */
case class MultiReply ( val items: Seq[MultiableReply] ) extends Reply

/**
 * An integer response
 */
case class IntReply ( val code: Int ) extends MultiableReply

/**
 * Null bulk response
 */
case class NullReply () extends MultiableReply

/**
 * String bulk response
 */
case class StringReply ( val str: String ) extends MultiableReply


