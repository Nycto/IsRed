package com.roundeights.isred

/**
 * An interface for building commands
 */
private [isred] object Cmd {

    /** Collects a list of args and generates a command */
    class Args ( private val args: List[Array[Byte]] ) {

        /** Adds an argument to this list */
        def :: ( arg: Any ): Args = arg match {
            case array: Array[Byte] => new Args( array :: args )
            case str: String => new Args( str.getBytes("UTF8") :: args )
            case (left, right) => left :: right :: this
            case seq: Seq[_] => seq.foldRight( this )( _ :: _ )
            case _ => new Args( arg.toString.getBytes("UTF8") :: args )
        }

        /** Finalizes a list of arguments */
        def ::: ( cmd: String ): Command = Command( cmd, args )

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
private[isred] case class Command (
    val command: String, val args: Seq[Array[Byte]] = Nil
) {

    /** Invokes a callback for each chunk of bytes in this command */
    def eachChunk ( callback: (Array[Byte]) => Unit ): Unit = {

        // Pushes an individual argument to the result
        def pushArg ( bytes: Array[Byte] ): Unit = {

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
        pushArg( command.getBytes("UTF8") )
        args.foreach( pushArg(_) )
    }

    /** Returns a string representation of the raw redis command */
    def protocolize: String = {
        val str = new StringBuilder
        eachChunk( (bytes) => str.append( new String(bytes, "UTF8") ) )
        str.toString
    }

}


