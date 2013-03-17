package com.roundeights.isred

import scala.concurrent.{Future, Promise, ExecutionContext}

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel._


/**
 * A wrapper for building Netty channels
 */
object Netty {

    /** Thrown when a Channel Future is cancelled */
    class Cancelled extends Exception

    /** Converts a channel future to a scala future */
    def futureify ( future: ChannelFuture ): Future[Channel] = {
        val promise = Promise[Channel]
        future.addListener(new ChannelFutureListener() {
            override def operationComplete( f: ChannelFuture ): Unit = {
                if ( f.isCancelled ) {
                    promise.failure( new Cancelled )
                } else if ( f.isSuccess ) {
                    promise.success( f.getChannel )
                } else {
                    promise.failure( f.getCause )
                }
            }
        })
        promise.future
    }

    /** The shared netty instance */
    private lazy val shared = new Netty

    /** Returns the shared Netty instance */
    def apply(): Netty = shared

}

/**
 * A Netty connection builder.
 */
class Netty ( channelFactory: ChannelFactory ) {

    /** The thread pools to use for netty clients */
    def this () = this( {
        val pool = Executors.newCachedThreadPool()
        new NioClientSocketChannelFactory(pool, pool)
    } )

    /** A partially built Netty connection */
    class Builder private[Netty] (
        private val address: InetSocketAddress,
        private val bootstrap: ClientBootstrap
    ) {

        /** Adds a pipeline to the eventual channel */
        def withPipeline ( callback: (ChannelPipeline) => Unit ): Builder = {
            callback( bootstrap.getPipeline )
            this
        }

        /** Adds a list of Channel Handlers to the pipeline */
        def add ( name: String, handler: ChannelHandler ): Builder = {
            bootstrap.getPipeline.addLast( name, handler )
            this
        }

        /** Builds the netty client */
        def build: Future[Channel]
            = Netty.futureify( bootstrap.connect(address) )

    }

    /** Initializes a connection to the given address */
    def open ( address: InetSocketAddress ): Builder = {
        val bootstrap = new ClientBootstrap( channelFactory )
        bootstrap.setPipeline( Channels.pipeline )
        bootstrap.setOption("tcpNoDelay", true)
        bootstrap.setOption("keepAlive", true)

        new Builder( address, bootstrap )
    }

    /** Initializes a connection to the given address */
    def open ( host: String, port: Int ): Builder
        = open( new InetSocketAddress(host, port) )

    /** Shuts down all the resources associated with this instace */
    def shutdown (implicit context: ExecutionContext): Unit = {
        context.execute(new Runnable {
            override def run: Unit = channelFactory.releaseExternalResources
        })
    }

}


