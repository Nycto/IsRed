package com.roundeights.isred

import org.specs2.mutable._

class ParserTest extends Specification {

    "The Parser" should {

        "Parse a Success Status Code" in {
            Parser().parse("+OK\r\n").get must_== SuccessReply("OK")
        }

        "Parse an Error Status Code" in {
            Parser().parse("-ERR unknown command 'foobar'\r\n").get must_==
                FailureReply("ERR", "unknown command 'foobar'")
        }

        "Parse an Integer" in {
            Parser().parse(":1000\r\n").get must_== IntReply(1000)
        }

        "Parse a Bulk value" in {
            Parser().parse("$6\r\nfoobar\r\n").get must_==
                StringReply("foobar")
        }

        "Parse a Null value" in {
            Parser().parse("$-1\r\n").get must_== NullReply()
        }

        "Parse a Multi-bulk value" in {
            Parser().parse(
                "*5\r\n:1\r\n:2\r\n:3\r\n:4\r\n$6\r\nfoobar\r\n"
            ).get must_== MultiReply(
                IntReply(1), IntReply(2), IntReply(3),
                IntReply(4), StringReply("foobar")
            )
        }

        "Parse an empty Multi-bulk value" in {
            Parser().parse("*0\r\n").get must_== MultiReply()
        }

        "Parse a null Multi-bulk value" in {
            Parser().parse("*-1\r\n").get must_== NullReply()
        }

        "Parse a Multi-bulk value containing nulls" in {
            Parser().parse(
                "*3\r\n$3\r\nfoo\r\n$-1\r\n$3\r\nbar\r\n"
            ).get must_== MultiReply(
                StringReply("foo"), NullReply(), StringReply("bar")
            )
        }

    }

}


