package com.roundeights.isred

/**
 * Methods for sending connection level requests
 */
trait Connection extends Iface {

    /** Authenticates to the server with a password */
    def auth ( password: String ): AckResult
        = getAck( "AUTH" ::: password :: Cmd() )

    /** Echos back the given string */
    def echo ( message: String ): BulkResult[String]
        = getBulk( "ECHO" ::: message :: Cmd() )

    /** Pings the server */
    def ping: AckResult = getAck( Cmd("PING") )

    /** Closes the connection to the server */
    def quit: AckResult = getAck( Cmd("QUIT") )

    /** Selects a database */
    def select ( db: Int ): AckResult
        = getAck( "SELECT" ::: db :: Cmd() )
}


