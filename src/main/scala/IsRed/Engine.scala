package com.roundeights.isred

import scala.concurrent.{Future, ExecutionContext}
import org.jboss.netty.channel._

/**
 * An interface for sending commands and reading replies
 */
abstract class Engine (
    val host: String,
    val port: Int = 6380
) {

    /** The netty resources */
    private val netty = Netty()

    /** Returns a netty channel */
    private def getChannel: Future[Channel] = {
        netty.open(host, port).tcpNoDelay.keepAlive.connect
    }

    /** Shuts down all the resources associated with this instace */
    def shutdown (implicit context: ExecutionContext): Unit = netty.shutdown

    /** Sends the given command over the wire */
    def send ( command: Command ): Future[Reply]

}
