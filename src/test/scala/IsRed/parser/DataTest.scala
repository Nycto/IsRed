package com.roundeights.isred

import org.specs2.mutable._

class IntParserTest extends Specification {

    "An Int parser" should {

        "Generate a IntReply instance" in {
            val parser = new IntParser

            parser.parse(":123\r\n", 1) must_==
                Parser.Complete( 5, IntReply(123) )
        }

        "Handle multiple chunks" in {
            val parser = new IntParser

            parser.parse(":1", 1) must_== Parser.Incomplete(1)
            parser.parse("") must_== Parser.Incomplete(0)
            parser.parse("2345\r\n") must_==
                Parser.Complete( 6, IntReply(12345) )
        }

        "Throw an exception when the number is invalid" in {
            val parser = new IntParser
            parser.parse(":Peanut\r\n", 1) must throwA[NumberFormatException]
        }

    }

}


