package com.roundeights.isred

import scala.concurrent.{Future, Promise, ExecutionContext}
import org.jboss.netty.channel._
import org.jboss.netty.buffer._


/**
 * Encodes a command as a ChannelBuffer
 */
protected class CommandEncoder extends ChannelDownstreamHandler {

    /** {@inheritDoc} */
    override def handleDownstream (
        context: ChannelHandlerContext, event: ChannelEvent
    ): Unit = event match {
        case msg: MessageEvent => {
            msg.getMessage match {
                case cmd: Command => cmd.eachChunk {
                    chunk => context.sendDownstream(
                        new UpstreamMessageEvent(
                            msg.getChannel,
                            ChannelBuffers.wrappedBuffer( chunk ),
                            msg.getRemoteAddress
                        )
                    )
                }
                case value => context.sendDownstream( msg )
            }
        }
        case _ => context.sendDownstream( event )
    }

}

/**
 * Decods a reply from Redis
 */
protected class ReplyDecoder  extends SimpleChannelUpstreamHandler {

    /** The internal result of this decoder instance */
    private val result = Promise[Reply]()

    /** The parser */
    private val parser = Parser()

    /** The future into which the reply will be written */
    def future = result.future

    /** {@inheritDoc} */
    override def messageReceived(
        context: ChannelHandlerContext, event: MessageEvent
    ): Unit = event.getMessage match {
        case buf: ChannelBuffer =>
            parser.parse( buf.array ).foreach( result.success( _ ) )
        case _ => context.sendUpstream( event )
    }

    /** {@inheritDoc} */
    override def exceptionCaught(
        context: ChannelHandlerContext, event: ExceptionEvent
    ) = result.failure( event.getCause )
}

/**
 * A pool of Netty Channels
 */
protected class ChannelPool ( host: String, port: Int, maxConnect: Int ) {

    /** The netty resources */
    private val netty = Netty()

    /** The base instance to use for building new channels */
    private val builder = netty
            .open(host, port)
            .tcpNoDelay.keepAlive
            .add( "encoder" -> new CommandEncoder )
            .add( "decoder" -> new ChannelUpstreamHandler {
                override def handleUpstream(
                    context: ChannelHandlerContext, event: ChannelEvent
                ): Unit = context.sendUpstream( event )
            })

    /** Shuts down all the resources associated with this instace */
    def shutdown (implicit context: ExecutionContext): Unit = netty.shutdown

    /** Returns a netty channel */
    def getChannel: Future[Channel] = {
        builder.connect
    }
}

/**
 * An interface for sending commands and reading replies
 */
class Engine ( val host: String, val port: Int, val maxConnect: Int ) {

    /** The pool of channels */
    private val pool = new ChannelPool( host, port, maxConnect )

    /** Shuts down all the resources associated with this instace */
    def shutdown (implicit context: ExecutionContext): Unit = pool.shutdown

    /** Sends the given command over the wire */
    def send
        ( command: Command )
        (implicit context: ExecutionContext)
    : Future[Reply] = {
        pool.getChannel.flatMap { chan => {
            val parser = new ReplyDecoder
            val pipeline = chan.getPipeline

            pipeline.addLast( "parser", parser )
            parser.future.onComplete { _ => pipeline.remove( parser ) }

            chan.write( command )

            parser.future
        }}
    }

}

