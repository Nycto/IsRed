package com.roundeights.isred

import scala.concurrent.{Future, Promise, ExecutionContext}
import org.jboss.netty.channel._
import org.jboss.netty.buffer._

/**
 * An interface to which commands can be sent
 */
trait Sendable {

    /** Shuts down all the resources associated with this instace */
    def close: Unit

    /** Sends the given command over the wire */
    def send ( command: Command ): Future[Reply]
}

/**
 * Encodes a command as a ChannelBuffer
 */
private[isred] class CommandEncoder extends ChannelDownstreamHandler {

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
private[isred] class ReplyDecoder  extends SimpleChannelUpstreamHandler {

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
 * An individual connection
 */
class RedisChannel
    ( private val channel: Channel )
    ( implicit context: ExecutionContext )
extends Sendable {

    /** The queue of operations to execute */
    private val work = new WorkList[Reply]

    /** Closes this connection */
    override def close: Unit = channel.close

    /** Sends the given command over the wire */
    override def send ( command: Command ): Future[Reply] = work.run {

        // Create a new parser and add it to the pipeline for this channel
        val parser = new ReplyDecoder
        val pipeline = channel.getPipeline
        pipeline.addLast( "parser", parser )

        // Ordering is important here. We need to make sure the parsing
        // pipeline is removed before this channel is returned to the pool.
        // Thus, we create a new promise that we manually complete
        val result = Promise[Reply]()
        parser.future.onComplete( parsed => {
            pipeline.remove( parser )
            result.complete( parsed )
        })

        // Once the parser is attached to the pipeline, we are ready to
        // receive data, which means we are ready to WRITE data
        channel.write( command )

        result.future
    }
}

/**
 * A pool of Netty Channels
 */
private[isred] class ChannelPool (
    host: String, port: Int,
    maxConnect: Int, connectTimeout: Int,
    onConnect: (RedisChannel) => Future[_]
)(
    implicit context: ExecutionContext
) {

    /** The netty resources */
    private val netty = Netty()

    /** Shutdown the Netty connections when the JVM exits */
    sys.addShutdownHook { netty.shutdown }

    /** The base instance to use for building new channels */
    private val builder = netty
            .open(host, port)
            .tcpNoDelay.keepAlive
            .connectTimeout( connectTimeout )
            .add( "encoder" -> new CommandEncoder )
            .add( "decoder" -> new ChannelUpstreamHandler {
                override def handleUpstream(
                    context: ChannelHandlerContext, event: ChannelEvent
                ): Unit = context.sendUpstream( event )
            })

    /** The pool of open connections */
    private val pool = Pool[RedisChannel](
        max = maxConnect,
        builder = () => {
            builder.connect.flatMap( nettyChan => {
                val chan = new RedisChannel( nettyChan )
                onConnect(chan).map( _ => chan )
            } )
        },
        onRetire = (conn: RedisChannel) => conn.close
    )

    /** Shuts down all the resources associated with this instace */
    def shutdown: Unit = netty.shutdown

    /** Returns a netty channel */
    def flatMap( callback: RedisChannel => Future[Reply] ): Future[Reply]
        = pool.flatMap( callback )
}

/**
 * An interface for sending commands and reading replies
 */
private[isred] class Engine (
    host: String, port: Int,
    maxConnect: Int, connectTimeout: Int,
    onConnect: (Sendable) => Future[_]
)(
    implicit context: ExecutionContext
) extends Sendable {

    /** The pool of channels */
    private val pool = new ChannelPool(
        host, port, maxConnect, connectTimeout, onConnect
    )

    /** Shuts down all the resources associated with this instace */
    override def close: Unit = pool.shutdown

    /** Sends a single command over the wire */
    override def send ( command: Command ): Future[Reply]
        = pool.flatMap( _.send(command) )

}

