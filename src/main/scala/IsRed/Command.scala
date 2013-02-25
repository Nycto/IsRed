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

        /** Adds a new argument to the left of this argument list */
        def :: ( arg: Any ): Args = arg match {
            case str: String => new Args( str :: args )
            case (left, right) => left :: right :: this
            case seq: Seq[_] => seq.foldRight( this )( _ :: _ )
            case _ => new Args( arg.toString :: args )
        }

        /** Finalizes a list of arguments */
        def ::: ( cmd: Command ): Command = Command( cmd.command, args )

    }

    /** Returns a new command */
    def apply( command: String ) = new Command( command )

    /** Returns a new arg builder */
    def apply() = new Args( Nil )

}


