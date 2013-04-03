package com.roundeights.isred

import org.specs2.mutable._

class ResponseTest extends Specification {

    "Building simple types from responses" should {

        "produce an Ack" in {
            SuccessReply("OK").asAck must_== true
            FailureReply("Err", "Oops").asAck must throwA[ReplyError]
            IntReply(0).asAck must throwA[UnexpectedReply]
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

    }

}

