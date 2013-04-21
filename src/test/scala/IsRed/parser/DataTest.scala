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

class StringParserTest extends Specification {

    "A String parser" should {

        "Generate a StringReply instance" in {
            val parser = new StringParser

            parser.parse("$7\r\nTesting\r\n", 1) must_==
                Parser.Complete( 12, StringReply("Testing") )
        }

        "Handle multiple chunks" in {
            val parser = new StringParser

            parser.parse("$7", 1) must_== Parser.Incomplete(1)
            parser.parse("") must_== Parser.Incomplete(0)
            parser.parse("\r\nTest") must_== Parser.Incomplete(6)
            parser.parse("ing\r\n") must_==
                Parser.Complete( 5, StringReply("Testing") )
        }

        "Parse zero length strings" in {
            val parser = new StringParser

            parser.parse("$0\r\n\r\n", 1) must_==
                Parser.Complete( 5, StringReply("") )
        }

        "Parse Null Responses" in {
            val parser = new StringParser

            parser.parse("$-1\r\n", 1) must_==
                Parser.Complete( 4, NullReply() )
        }

        "Parse a binary centric bulk reply" in {
            val parser = new StringParser

            parser.parse(
                0x31, 0x37, 0x0d, 0x0a,
                0x00, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x06, 0x00,
                0x17, 0x1b, 0xa9, 0xb8, 0x34, 0xff, 0xa7, 0xfd,
                0x0d, 0x0a
            ) must_== Parser.Complete( 23, StringReply(
                0x00, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x06, 0x00,
                0x17, 0x1b, 0xa9, 0xb8, 0x34, 0xff, 0xa7, 0xfd
            ))
        }

    }

}

class MultiParserTest extends Specification {

    "A Multi parser" should {

        "Generate an empty list when the arg length is 0" in {
            val parser = new MultiParser

            parser.parse("*0\r\n", 1) must_==
                Parser.Complete( 3, MultiReply() )
        }

        "Generate a Null reply when the arg length is < 1" in {
            val parser = new MultiParser

            parser.parse("*-1\r\n", 1) must_==
                Parser.Complete( 4, NullReply() )
        }

        "Generate a list of Bulk replies" in {
            val parser = new MultiParser

            parser.parse(
                "*3\r\n" +
                "$-1\r\n" +
                "$3\r\narg\r\n" +
                ":123\r\nextra",
                1
            ) must_== Parser.Complete( 23, MultiReply(
                NullReply(), StringReply("arg"), IntReply(123)
            ))
        }

        "Handle data spread across multiple chunks" in {
            val parser = new MultiParser

            parser.parse("*3\r", 1) must_== Parser.Incomplete(2)
            parser.parse("\n$-1\r\n$") must_== Parser.Incomplete(7)
            parser.parse("3\r\narg\r\n") must_== Parser.Incomplete(8)
            parser.parse(":123\r\n") must_==
                Parser.Complete( 6, MultiReply(
                    NullReply(), StringReply("arg"), IntReply(123)
                ))
        }

    }

}

