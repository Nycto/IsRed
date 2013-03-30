package com.roundeights.isred

import org.specs2.mutable._

class ParseUntilTest extends Specification {

    "Parsing with a single byte delimiter" should {

        "generate a full response when it finds the delimiter" in {
            val parser = new ParseUntil(
                "\n".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Test\n".getBytes("UTF8") ) must_==
                Parser.Complete( 5, "Test" )
        }

        "generate partial responses until it finds the delimiter" in {
            val parser = new ParseUntil(
                "\n".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testing".getBytes("UTF8") ) must_==
                Parser.Incomplete(7)

            parser.parse( " a chunked ".getBytes("UTF8") ) must_==
                Parser.Incomplete(11)

            parser.parse( "Response\n".getBytes("UTF8") ) must_==
                Parser.Complete( 9, "Testing a chunked Response" )
        }

    }

    "Parsing with a multi-byte delimiter" should {

        "generate a full response when it finds the delimiter" in {
            val parser = new ParseUntil(
                "abc".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testabc".getBytes("UTF8") ) must_==
                Parser.Complete( 7, "Test" )
        }

        "generate partial responses until it finds the delimiter" in {
            val parser = new ParseUntil(
                "abc".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testing".getBytes("UTF8") ) must_==
                Parser.Incomplete(7)

            parser.parse( " a chunked ".getBytes("UTF8") ) must_==
                Parser.Incomplete(11)

            parser.parse( "Responseabc".getBytes("UTF8") ) must_==
                Parser.Complete( 11, "Testing a chunked Response" )
        }

        "handle a delimiter that spans two chunks" in {
            val parser = new ParseUntil(
                "abc".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testinga".getBytes("UTF8") ) must_==
                Parser.Incomplete(8)

            parser.parse( "bc".getBytes("UTF8") ) must_==
                Parser.Complete( 2, "Testing" )
        }

        "handle a delimiter that spans three chunks" in {
            val parser = new ParseUntil(
                "abc".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testing".getBytes("UTF8") ) must_==
                Parser.Incomplete(7)

            parser.parse( "a".getBytes("UTF8") ) must_==
                Parser.Incomplete(1)

            parser.parse( "b".getBytes("UTF8") ) must_==
                Parser.Incomplete(1)

            parser.parse( "c".getBytes("UTF8") ) must_==
                Parser.Complete( 1, "Testing" )
        }

        "Recover from partial delimiter matches" in {
            val parser = new ParseUntil(
                "abc".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testabababc".getBytes("UTF8") ) must_==
                Parser.Complete( 11, "Testabab" )
        }

        "Recover from partial delimiter matches spanning chunks" in {
            val parser = new ParseUntil(
                "abc".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Test ab".getBytes("UTF8") ) must_==
                Parser.Incomplete(7)

            parser.parse( "outabc".getBytes("UTF8") ) must_==
                Parser.Complete( 6, "Test about" )
        }

    }

}


