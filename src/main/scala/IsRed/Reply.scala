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

    /** Convert to a Key */
    implicit def reply2key ( reply: Reply ) = Key( reply.asString )

    /** Convert to a KeyType */
    implicit def reply2keytype ( reply: Reply )
        = KeyType.fromString( reply.asString )

    /** Convert to a byte array */
    implicit def reply2bytes ( reply: Reply ) = reply.asBytes
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

    /** Returns this value as a sequence */
    def asSeq: Seq[MultiableReply] = throw UnexpectedReply("Sequence", this)

    /** Returns this value a key type */
    def asKeyType: KeyType.Type = throw UnexpectedReply("KeyType", this)

    /** Returns this value as a list of bytes */
    def asBytes: Array[Byte] = throw UnexpectedReply("Byte Array", this)
}

/**
 * A Success Code response
 */
case class SuccessReply ( val code: String ) extends Reply {

    /** {@inheritDoc} */
    override def asAck: Boolean = true

    /** {@inheritDoc} */
    override def asBool: Boolean = true

    /** {@inheritDoc} */
    override def asKeyType: KeyType.Type = KeyType.fromString( code )
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

    /** {@inheritDoc} */
    override def asKeyType: KeyType.Type = throwErr

    /** {@inheritDoc} */
    override def asBytes: Array[Byte] = throwErr
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

    /** {@inheritDoc} */
    override def asBytes: Array[Byte]
        = java.nio.ByteBuffer.allocate(4).putInt( asInt ).array
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

    /** {@inheritDoc} */
    override def asBytes: Array[Byte] = new Array[Byte](0)
}

/** Companion */
object StringReply {

    /** Creates a string reply from a string */
    def apply ( bytes: Array[Byte] ): StringReply = new StringReply( bytes )

    /** Creates a string reply from a string */
    def apply ( str: String ): StringReply = apply( str.getBytes("UTF8") )

    /** Creates a string reply from list of ints */
    def apply ( bytes: Int* ): StringReply
        = apply( bytes.map( _.toByte ).toArray )
}

/**
 * String bulk response
 */
class StringReply (
    override val asBytes: Array[Byte]
) extends MultiableReply with Equals {

    /** {@inheritDoc} */
    override def asString: String = Parser.readable( asBytes )

    /** {@inheritDoc} */
    override def asInt: Int = asString.toInt

    /** {@inheritDoc} */
    override def asDouble: Double = asString.toDouble

    /** {@inheritDoc} */
    override def toString = "StringReply(%s)".format( asString )

    /** {@inheritDoc} */
    override def canEqual( other: Any ) = other.isInstanceOf[StringReply]

    /** {@inheritDoc} */
    override def equals( other: Any ): Boolean = other match {
        case that: StringReply if that.canEqual( this )
            => asBytes.deep == that.asBytes.deep
        case _ => false
    }

    /** {@inheritDoc} */
    override def hashCode: Int = asBytes.foldLeft(41)( _ * 41 + _ )
}


