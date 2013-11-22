package com.roundeights.isred

/**
 * Methods for interacting with Redis Scripting methods
 */
trait Scripting extends Iface {

    /** Execute a Lua script server side */
    def eval[A: Convert](
        script: String, keys: Seq[Key] = Nil, args: Seq[String] = Nil
    ): AnyResult[A] = getAny[A](
        "EVAL" ::: script :: keys.length :: keys :: args :: Cmd()
    )

    /** Execute a Lua script server side */
    def evalSha[A: Convert](
        sha1: String, keys: Seq[Key], args: Seq[String]
    ): AnyResult[A] = getAny[A](
        "EVALSHA" ::: sha1 :: keys.length :: keys :: args :: Cmd()
    )

    /** Check existence of a single script in the script cache. */
    def scriptExists ( sha1s: String* ): BulkSeqResult[Boolean]
        = getBulkSeq[Boolean]( "SCRIPT" ::: "EXISTS" :: sha1s :: Cmd() )

    /** Remove all the scripts from the script cache. */
    def scriptFlush: AckResult = getAck( "SCRIPT" ::: "FLUSH" :: Cmd() )

    /** Kill the script currently in execution. */
    def scriptKill: AckResult = getAck( "SCRIPT" ::: "KILL" :: Cmd() )

    /** Load the specified Lua script */
    def scriptLoad ( script: String ): BulkResult[String]
        = getBulk[String]( "SCRIPT" ::: "LOAD" :: script :: Cmd() )
}

