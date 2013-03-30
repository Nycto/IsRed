package com.roundeights.isred

import org.specs2.mutable._

class ParseUntilTest extends Specification {

    "Parsing an empty byte array" should {

        "Return an incomplete result" in {
            val parser = new ParseUntil(
                "\n".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( new Array(0) ) must_== Parser.Incomplete( 0 )
        }

    }

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

    "Parsing with an initial offset" should {

        "Skip the first few bytes" in {
            val parser = new ParseUntil(
                "\n".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testing\n".getBytes("UTF8"), 2 ) must_==
                Parser.Complete( 6, "sting" )
        }

        "Return an empty incomplete when the start is beyond the length" in {
            val parser = new ParseUntil(
                "\n".getBytes("UTF8"),
                new String(_, "UTF8")
            )

            parser.parse( "Testing\n".getBytes("UTF8"), 50 ) must_==
                Parser.Incomplete( 0 )
        }

    }

}

class ParseLengthTest extends Specification {

    "Parsing a the entire amount in one chunk" should {

        "Return the completed result" in {
            val parser = new ParseLength( 5, new String(_, "UTF8") )

            parser.parse( "Hello!".getBytes("UTF8") ) must_==
                Parser.Complete( 5, "Hello" )

            parser.parse( "Extra".getBytes("UTF8") ) must_==
                Parser.Complete( 0, "Hello" )
        }

    }

    "Parsing across multipe chunks" should {

        "Return incomplete results until it's fulfilled" in {
            val parser = new ParseLength( 16, new String(_, "UTF8") )

            parser.parse( "There ".getBytes("UTF8") ) must_==
                Parser.Incomplete( 6 )

            parser.parse( "are ".getBytes("UTF8") ) must_==
                Parser.Incomplete( 4 )

            parser.parse( "Chunks".getBytes("UTF8") ) must_==
                Parser.Complete( 6, "There are Chunks" )
        }

    }

    "Parsing for a 0 length result" should {

        "Returns Complete when content is passed" in {
            val parser = new ParseLength( 0, new String(_, "UTF8") )

            parser.parse( "There ".getBytes("UTF8") ) must_==
                Parser.Complete(0, "")
        }

        "Returns Complete when no content is passed" in {
            val parser = new ParseLength( 0, new String(_, "UTF8") )

            parser.parse( new Array(0) ) must_==
                Parser.Complete(0, "")
        }

    }

    "Parsing with a start value" should {

        "ignore the initial bytes" in {
            val parser = new ParseLength( 4, new String(_, "UTF8") )

            parser.parse( "Data Point".getBytes("UTF8"), 2 ) must_==
                Parser.Complete( 4, "ta P" )
        }

        "Not parse anything when the start is greater than the input" in {
            val parser = new ParseLength( 4, new String(_, "UTF8") )

            parser.parse( "Data Point".getBytes("UTF8"), 20 ) must_==
                Parser.Incomplete(0)
        }

        "Assume a negative value means 0" in {
            val parser = new ParseLength( 4, new String(_, "UTF8") )

            parser.parse( "Data".getBytes("UTF8"), -10 ) must_==
                Parser.Complete( 4, "Data" )
        }

    }

}


