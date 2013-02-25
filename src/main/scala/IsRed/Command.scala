package com.roundeights.isred

/**
 * A Redis command
 */
case class Command ( val command: String, val args: Seq[String] = Nil )

/**
 * An interface for building commands
 */
object Cmd {

    /** Collects a list of args and generates a command */
    class Args ( private val args: List[String] ) {

        /** Adds a string */
        def :: ( arg: String ): Args = new Args( arg :: args )

        /** Adds a key */
        def :: ( arg: Key ): Args = new Args( arg.toString :: args )

        /** Adds an Int */
        def :: ( arg: Int ): Args = new Args( arg.toString :: args )

        /** Adds an Int */
        def :: ( arg: Seq[Key] ): Args = arg.foldRight( this )( _ :: _ )

        /** Finalizes a list of arguments */
        def :: ( cmd: Command ): Command = Command( cmd.command, args )

    }

    /** Returns a new command */
    def apply( command: String ) = new Command( command )

    /** Returns a new arg builder */
    def apply() = new Args( Nil )

}


