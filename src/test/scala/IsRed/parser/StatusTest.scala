package com.roundeights.isred

import org.specs2.mutable._

class StatusParserTest extends Specification {

    "A Success parser" should {

        "Generate a SuccessReply instance" in {
            val parser = new SuccessParser

            parser.parse("+OK\r\n", 1) must_==
                Parser.Complete( 4, SuccessReply("OK") )
        }

        "Handle multiple chunks" in {
            val parser = new SuccessParser

            parser.parse("+O", 1) must_== Parser.Incomplete(1)
            parser.parse("") must_== Parser.Incomplete(0)
            parser.parse("K\r\n") must_==
                Parser.Complete( 3, SuccessReply("OK") )
        }

    }

    "A Failure parser" should {

        "Generate a FailureReply instance" in {
            val parser = new FailureParser

            parser.parse("-OOPS That's bad.\r\n", 1) must_==
                Parser.Complete( 18, FailureReply("OOPS", "That's bad.") )
        }

        "Handle multiple chunks" in {
            val parser = new FailureParser

            parser.parse("-OOP", 1) must_== Parser.Incomplete(3)
            parser.parse("") must_== Parser.Incomplete(0)
            parser.parse("S That's") must_== Parser.Incomplete(8)
            parser.parse(" bad.\r\n") must_==
                Parser.Complete( 7, FailureReply("OOPS", "That's bad.") )
        }

    }

}

