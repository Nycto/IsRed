package com.roundeights.isred

import org.specs2.mutable._

class CommandTest extends Specification {

    "A Command converted to the redis protocol" should {

        "Handle a command with no arguments" in {
            Cmd("SET").protocolize must_== "*1\r\n$3\r\nSET\r\n"
        }

        "Handle a command with arguments" in {
            val cmd = "GOOBER" ::: "one" :: 278 :: 3.14 :: Cmd()
            cmd.protocolize must_== (
                "*4\r\n" +
                "$6\r\nGOOBER\r\n" +
                "$3\r\none\r\n" +
                "$3\r\n278\r\n" +
                "$4\r\n3.14\r\n"
            )
        }

        "Add a byte array" in {
            ( "BLAH" ::: "test".getBytes :: Cmd() ).protocolize must_== (
                "*2\r\n" +
                "$4\r\nBLAH\r\n" +
                "$4\r\ntest\r\n"
            )
        }

        "Handle a an empty string argument" in {
            ( "BLAH" ::: "" :: Cmd() ).protocolize must_== (
                "*2\r\n" +
                "$4\r\nBLAH\r\n" +
                "$0\r\n\r\n"
            )
        }

        "Add each element in a sequence" in {
            ( "BLAH" ::: List(1, 2, 3) :: Cmd() ).protocolize must_== (
                "*4\r\n" +
                "$4\r\nBLAH\r\n" +
                "$1\r\n1\r\n" +
                "$1\r\n2\r\n" +
                "$1\r\n3\r\n"
            )
        }

        "Add both elements in a tuple" in {
            ( "BLAH" ::: (1 -> 2) :: Cmd() ).protocolize must_== (
                "*3\r\n" +
                "$4\r\nBLAH\r\n" +
                "$1\r\n1\r\n" +
                "$1\r\n2\r\n"
            )
        }

    }

}

