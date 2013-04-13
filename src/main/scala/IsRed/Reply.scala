package com.roundeights.isred

import scala.language.implicitConversions

/** Reply companion */
object Reply {

    /** Convert to an Boolean */
    implicit def reply2bool ( reply: Reply ) = reply.asBool

    /** Convert to an Int */
    implicit def reply2int ( reply: Reply ) = reply.asInt

    /** Convert to a Double */
    implicit def reply2double ( reply: Reply ) = reply.asDouble

    /** Convert to a float */
    implicit def reply2float ( reply: Reply ) = reply.asDouble.toFloat

    /** Convert to a String */
    implicit def reply2string ( reply: Reply ) = reply.asString

}


/**
 * A Raw response from Redis
 */
sealed trait Reply {

    /** Returns this value as Boolean True, or throws */
    def asAck: Boolean = throw UnexpectedReply("Ack", this)

    /** Returns this value as an Integer */
    def asInt: Int = throw UnexpectedReply("Int", this)

    /** Returns this value as a Float */
    def asDouble: Double = throw UnexpectedReply("Double", this)

    /** Returns this value as a Boolean */
    def asBool: Boolean = throw UnexpectedReply("Boolean", this)

    /** Returns this value as a String */
    def asString: String = throw UnexpectedReply("String", this)

    /** Returns this value as as sequence */
    def asSeq: Seq[MultiableReply] = throw UnexpectedReply("Sequence", this)
}

/**
 * A Success Code response
 */
case class SuccessReply ( val code: String ) extends Reply {

    /** {@inheritDoc} */
    override def asAck: Boolean = true

    /** {@inheritDoc} */
    override def asBool: Boolean = true
}

/**
 * A Failed response
 */
case class FailureReply (
    val code: String, val message: String
) extends Reply {

    /** Throws the error this failure reply represents */
    def throwErr: Nothing = throw ReplyError(code, message)

    /** {@inheritDoc} */
    override def asAck: Boolean = throwErr

    /** {@inheritDoc} */
    override def asInt: Int = throwErr

    /** {@inheritDoc} */
    override def asDouble: Double = throwErr

    /** {@inheritDoc} */
    override def asBool: Boolean = throwErr

    /** {@inheritDoc} */
    override def asString: String = throwErr

    /** {@inheritDoc} */
    override def asSeq: Seq[MultiableReply] = throwErr
}

/**
 * Multi-able responses can be returned as part of a MultiReply
 */
sealed trait MultiableReply extends Reply

/**
 * A aggregating response for returning multiple responses
 */
case class MultiReply ( override val asSeq: MultiableReply* ) extends Reply

/**
 * An integer response
 */
case class IntReply ( override val asInt: Int ) extends MultiableReply {

    /** {@inheritDoc} */
    override def asAck: Boolean = asInt match {
        case 1 => true
        case 0 => throw UnsuccessfulReply(this)
        case _ => throw UnexpectedReply("0 or 1", this)
    }

    /** {@inheritDoc} */
    override def asDouble: Double = asInt

    /** {@inheritDoc} */
    override def asBool: Boolean = (asInt != 0)

    /** {@inheritDoc} */
    override def asString: String = asInt.toString
}

/**
 * Null bulk response
 */
case class NullReply () extends MultiableReply {

    /** {@inheritDoc} */
    override def asInt: Int = 0

    /** {@inheritDoc} */
    override def asDouble: Double = 0

    /** {@inheritDoc} */
    override def asString: String = ""
}

/**
 * String bulk response
 */
case class StringReply (
    override val asString: String
) extends MultiableReply {

    /** {@inheritDoc} */
    override def asInt: Int = asString.toInt

    /** {@inheritDoc} */
    override def asDouble: Double = asString.toDouble
}

