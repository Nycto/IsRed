package com.roundeights.isred

import org.specs2.mutable._

class ReplyTest extends Specification {

    "Building simple types from responses" should {

        "produce an Ack" in {
            SuccessReply("OK").asAck must_== true
            FailureReply("Err", "Oops").asAck must throwA[ReplyError]
            IntReply(0).asAck must throwA[UnsuccessfulReply]
            IntReply(1).asAck must_== true
            IntReply(23).asAck must throwA[UnexpectedReply]
            NullReply().asAck must throwA[UnexpectedReply]
            StringReply("Test").asAck must throwA[UnexpectedReply]
            MultiReply( IntReply(1) ).asAck must throwA[UnexpectedReply]
        }

        "produce an Int" in {
            SuccessReply("OK").asInt must throwA[UnexpectedReply]
            FailureReply("Err", "Oops").asInt must throwA[ReplyError]
            IntReply(123).asInt must_== 123
            NullReply().asInt must_== 0
            StringReply("Test").asInt must throwA[NumberFormatException]
            StringReply("123").asInt must_== 123
            MultiReply( IntReply(1) ).asInt must throwA[UnexpectedReply]
        }

        "produce a Double" in {
            SuccessReply("OK").asDouble must throwA[UnexpectedReply]
            FailureReply("Err", "Oops").asDouble must throwA[ReplyError]
            IntReply(123).asDouble must_== 123
            NullReply().asDouble must_== 0
            StringReply("Test").asDouble must throwA[NumberFormatException]
            StringReply("3.1415").asDouble must_== 3.1415
            MultiReply( IntReply(1) ).asDouble must throwA[UnexpectedReply]
        }

        "produce a Boolean" in {
            SuccessReply("OK").asBool must_== true
            FailureReply("Err", "Oops").asBool must throwA[ReplyError]
            IntReply(1).asBool must_== true
            IntReply(0).asBool must_== false
            NullReply().asBool must throwA[UnexpectedReply]
            StringReply("Test").asBool must throwA[UnexpectedReply]
            MultiReply( IntReply(1) ).asBool must throwA[UnexpectedReply]
        }

        "produce a String" in {
            SuccessReply("OK").asString must throwA[UnexpectedReply]
            FailureReply("Err", "Oops").asString must throwA[ReplyError]
            IntReply(123).asString must_== "123"
            NullReply().asString must_== ""
            StringReply("Test").asString must_== "Test"
            MultiReply( IntReply(1) ).asString must throwA[UnexpectedReply]
        }

        "produce a Sequence" in {
            SuccessReply("OK").asSeq must throwA[UnexpectedReply]
            FailureReply("Err", "Oops").asSeq must throwA[ReplyError]
            IntReply(1).asSeq must throwA[UnexpectedReply]
            IntReply(0).asSeq must throwA[UnexpectedReply]
            NullReply().asSeq must throwA[UnexpectedReply]
            StringReply("Test").asSeq must throwA[UnexpectedReply]
            MultiReply().asSeq must_== Seq()
            MultiReply( IntReply(1) ).asSeq must_== Seq( IntReply(1) )
        }

        "produce a KeyType" in {
            SuccessReply("string").asKeyType must_== KeyType.STRING
            SuccessReply("Hrm").asKeyType must throwA[IllegalArgumentException]
            FailureReply("Err", "Oops").asKeyType must throwA[ReplyError]
            IntReply(1).asKeyType must throwA[UnexpectedReply]
            NullReply().asKeyType must throwA[UnexpectedReply]
            StringReply("Test").asKeyType must throwA[UnexpectedReply]
            MultiReply().asKeyType must throwA[UnexpectedReply]
        }

        "produce a byte array" in {
            SuccessReply("OK").asBytes must throwA[UnexpectedReply]
            FailureReply("Err", "Oops").asBytes must throwA[ReplyError]
            StringReply("Test").asBytes.deep must_== "Test".getBytes.deep
            IntReply(123).asBytes.deep must_== Seq(0, 0, 0, 123).toArray.deep
            NullReply().asBytes must_== new Array(0)
            MultiReply( IntReply(1) ).asBytes must throwA[UnexpectedReply]
        }
    }

    "Implicitly casting a reply" should {

        "Convert to primitives" in {
            implicitly[Boolean]( IntReply(1) ) must_== true
            implicitly[Int]( IntReply(123) ) must_== 123
            implicitly[Double]( StringReply("3.1415") ) must_== 3.1415d
            implicitly[Float]( StringReply("3.1415") ) must_== 3.1415f
            implicitly[String]( StringReply("Test") ) must_== "Test"
        }

        "Convert to a key" in {
            implicitly[Key]( StringReply("Test") ) must_== Key("Test")

            implicitly[KeyType.Type]( StringReply("set") ) must_== KeyType.SET
        }

        "Convert to a byte array" in {
            implicitly[Array[Byte]]( StringReply("Test") ) must_==
                Array('T', 'e', 's', 't')
        }

        "Convert to sequences" in {
            implicitly[Seq[Boolean]]( MultiReply(
                IntReply(1), IntReply(0) ) ) must_==
                    Seq(true, false)

            implicitly[Seq[Int]]( MultiReply(
                IntReply(123), IntReply(456) ) ) must_==
                    Seq(123, 456)

            implicitly[Seq[Double]]( MultiReply(
                StringReply("3.1415"), StringReply("2.78") ) ) must_==
                    Seq( 3.1415d, 2.78d )

            implicitly[Seq[Float]]( MultiReply(
                StringReply("3.1415"), StringReply("2.78") ) ) must_==
                    Seq( 3.1415f, 2.78f )

            implicitly[Seq[String]]( MultiReply(
                StringReply("one"), StringReply("two") ) ) must_==
                    Seq("one", "two")
        }
    }

}

