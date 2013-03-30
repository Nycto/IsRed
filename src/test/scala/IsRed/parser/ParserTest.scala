package com.roundeights.isred

import org.specs2.mutable._

class ParseUntilTest extends Specification {

    // Converts a byte array to a string
    def asString ( bytes: Array[Byte] ) = new String(bytes, "UTF8")


    "Parsing an empty byte array" should {

        "Return an incomplete result" in {
            val parser = new ParseUntil( "\n", asString(_) )

            parser.parse( new Array[Byte](0) ) must_== Parser.Incomplete( 0 )
        }

    }

    "Parsing with a single byte delimiter" should {

        "generate a full response when it finds the delimiter" in {
            val parser = new ParseUntil( "\n", asString(_) )

            parser.parse( "Test\n" ) must_==
                Parser.Complete( 5, "Test" )
        }

        "generate partial responses until it finds the delimiter" in {
            val parser = new ParseUntil( "\n", asString(_) )

            parser.parse( "Testing" ) must_== Parser.Incomplete(7)

            parser.parse( " a chunked " ) must_== Parser.Incomplete(11)

            parser.parse( "Response\n" ) must_==
                Parser.Complete( 9, "Testing a chunked Response" )
        }

    }

    "Parsing with a multi-byte delimiter" should {

        "generate a full response when it finds the delimiter" in {
            val parser = new ParseUntil( "abc", asString(_) )

            parser.parse( "Testabc" ) must_== Parser.Complete( 7, "Test" )
        }

        "generate partial responses until it finds the delimiter" in {
            val parser = new ParseUntil( "abc", asString(_) )

            parser.parse( "Testing" ) must_== Parser.Incomplete(7)

            parser.parse( " a chunked " ) must_== Parser.Incomplete(11)

            parser.parse( "Responseabc" ) must_==
                Parser.Complete( 11, "Testing a chunked Response" )
        }

        "handle a delimiter that spans two chunks" in {
            val parser = new ParseUntil( "abc", asString(_) )

            parser.parse( "Testinga" ) must_== Parser.Incomplete(8)

            parser.parse( "bc" ) must_== Parser.Complete( 2, "Testing" )
        }

        "handle a delimiter that spans three chunks" in {
            val parser = new ParseUntil( "abc", asString(_) )

            parser.parse( "Testing" ) must_== Parser.Incomplete(7)

            parser.parse( "a" ) must_== Parser.Incomplete(1)

            parser.parse( "b" ) must_== Parser.Incomplete(1)

            parser.parse( "c" ) must_== Parser.Complete( 1, "Testing" )
        }

        "Recover from partial delimiter matches" in {
            val parser = new ParseUntil( "abc", asString(_) )

            parser.parse( "Testabababc" ) must_==
                Parser.Complete( 11, "Testabab" )
        }

        "Recover from partial delimiter matches spanning chunks" in {
            val parser = new ParseUntil( "abc", asString(_) )

            parser.parse( "Test ab" ) must_== Parser.Incomplete(7)

            parser.parse( "outabc" ) must_== Parser.Complete( 6, "Test about" )
        }

    }

    "Parsing with an initial offset" should {

        "Skip the first few bytes" in {
            val parser = new ParseUntil( "\n", asString(_) )

            parser.parse( "Testing\n", 2 ) must_==
                Parser.Complete( 6, "sting" )
        }

        "Return an empty incomplete when the start is beyond the length" in {
            val parser = new ParseUntil( "\n", asString(_) )

            parser.parse( "Testing\n", 50 ) must_== Parser.Incomplete( 0 )
        }

    }

}

class ParseLengthTest extends Specification {

    // Converts a byte array to a string
    def asString ( bytes: Array[Byte] ) = new String(bytes, "UTF8")


    "Parsing a the entire amount in one chunk" should {

        "Return the completed result" in {
            val parser = new ParseLength( 5, asString(_) )

            parser.parse( "Hello!" ) must_== Parser.Complete( 5, "Hello" )

            parser.parse( "Extra" ) must_== Parser.Complete( 0, "Hello" )
        }

    }

    "Parsing across multipe chunks" should {

        "Return incomplete results until it's fulfilled" in {
            val parser = new ParseLength( 16, asString(_) )

            parser.parse( "There " ) must_== Parser.Incomplete( 6 )

            parser.parse( "are " ) must_== Parser.Incomplete( 4 )

            parser.parse( "Chunks" ) must_==
                Parser.Complete( 6, "There are Chunks" )
        }

    }

    "Parsing for a 0 length result" should {

        "Return Complete when content is passed" in {
            val parser = new ParseLength( 0, asString(_) )

            parser.parse( "There " ) must_== Parser.Complete(0, "")
        }

        "Return Complete when no content is passed" in {
            val parser = new ParseLength( 0, asString(_) )

            parser.parse( new Array[Byte](0) ) must_== Parser.Complete(0, "")
        }

    }

    "Parsing with a start value" should {

        "Ignore the initial bytes" in {
            val parser = new ParseLength( 4, asString(_) )

            parser.parse( "Data Point", 2 ) must_==
                Parser.Complete( 4, "ta P" )
        }

        "Not parse anything when the start is greater than the input" in {
            val parser = new ParseLength( 4, asString(_) )

            parser.parse( "Data Point", 20 ) must_== Parser.Incomplete(0)
        }

        "Assume a negative value means 0" in {
            val parser = new ParseLength( 4, asString(_) )

            parser.parse( "Data", -10 ) must_== Parser.Complete( 4, "Data" )
        }

    }

}


class ParseChainTest extends Specification {

    // Converts a byte array to a string
    def asString ( bytes: Array[Byte] ) = new String(bytes, "UTF8")

    "Parsing a chain" should {

        "Handle data evenly split across multiple chunks" in {
            val parser = new ParseChain(
                new ParseLength( 5, asString(_) ),
                new ParseLength( 8, asString(_) )
            )

            parser.parse("Data!") must_== Parser.Incomplete(5)
            parser.parse("And Data") must_==
                Parser.Complete(8, ("Data!", "And Data"))
        }

        "Handle data extra data in the first chunk" in {
            val parser = new ParseChain(
                new ParseLength( 5, asString(_) ),
                new ParseLength( 8, asString(_) )
            )

            parser.parse("Data And") must_== Parser.Incomplete(8)
            parser.parse(" Data") must_==
                Parser.Complete(5, ("Data ", "And Data"))
        }

        "Handle missing data in the first chunk" in {
            val parser = new ParseChain(
                new ParseLength( 5, asString(_) ),
                new ParseLength( 8, asString(_) )
            )

            parser.parse("Da") must_== Parser.Incomplete(2)
            parser.parse("ta And Data") must_==
                Parser.Complete(11, ("Data ", "And Data"))
        }

        "Handle data in a single chunk" in {
            val parser = new ParseChain(
                new ParseLength( 5, asString(_) ),
                new ParseLength( 8, asString(_) )
            )

            parser.parse("Data And Data") must_==
                Parser.Complete(13, ("Data ", "And Data"))
        }

        "Handle a start offset" in {
            val parser = new ParseChain(
                new ParseLength( 5, asString(_) ),
                new ParseLength( 8, asString(_) )
            )

            parser.parse("Junk and Data And Data", 9) must_==
                Parser.Complete(13, ("Data ", "And Data"))
        }

        "Handle a chain in a chain" in {
            val parser = new ParseChain(
                new ParseLength( 4, asString(_) ),
                new ParseChain(
                    new ParseLength( 5, asString(_) ),
                    new ParseLength( 4, asString(_) )
                )
            )

            parser.parse("Data And Data") must_==
                Parser.Complete(13, ("Data", (" And ", "Data")))
        }

    }

}

