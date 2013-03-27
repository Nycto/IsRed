package com.roundeights.isred

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

    /** A few reusable character arrays */
    private[isred] val STAR = List[Byte]('*').toArray
    private[isred] val ENDLINE = List[Byte]('\r', '\n').toArray
    private[isred] val DOLLAR = List[Byte]('$').toArray

}

/**
 * A Redis command
 */
case class Command ( val command: String, val args: Seq[String] = Nil ) {

    /** Invokes a callback for each chunk of bytes in this command */
    def eachChunk ( callback: (Array[Byte]) => Unit ): Unit = {

        // Pushes an individual argument to the result
        def pushArg ( arg: String ): Unit = {
            val bytes = arg.getBytes("UTF8")

            // Push the number of bytes in this argument
            callback( Cmd.DOLLAR )
            callback( bytes.length.toString.getBytes("UTF8") )
            callback( Cmd.ENDLINE )

            // Push the content of the array
            callback( bytes )
            callback( Cmd.ENDLINE )
        }

        // Push the number of arguments
        callback( Cmd.STAR )
        callback( (args.size + 1).toString.getBytes("UTF8") )
        callback( Cmd.ENDLINE )

        // Push each argument
        pushArg( command )
        args.foreach( pushArg(_) )
    }

    /** Returns a string representation of the raw redis command */
    def protocolize: String = {
        val str = new StringBuilder
        eachChunk( (bytes) => str.append( new String(bytes, "UTF8") ) )
        str.toString
    }

}


