package com.roundeights.isred

import scala.concurrent.{Future, Promise, ExecutionContext}

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel._


/** Thrown when a Channel Future is cancelled */
class RequestCancelled extends Exception

/**
 * A wrapper for building Netty channels
 */
private[isred] object Netty {

    /** Converts a channel future to a scala future */
    def futureify ( future: ChannelFuture ): Future[Channel] = {
        val promise = Promise[Channel]
        future.addListener(new ChannelFutureListener() {
            override def operationComplete( f: ChannelFuture ): Unit = {
                if ( f.isCancelled ) {
                    promise.failure( new RequestCancelled )
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
    def apply(): Netty = new Netty

}

/**
 * A Netty connection builder.
 */
private[isred] class Netty ( channelFactory: ChannelFactory ) {

    /** The thread pools to use for netty clients */
    def this () = this( {
        val pool = Executors.newCachedThreadPool()
        new NioClientSocketChannelFactory(pool, pool)
    } )

    /** A partially built Netty connection */
    class Builder private[Netty] (
        private val address: InetSocketAddress,
        private val pipeline: List[(String, ChannelHandler)] = List(),
        private val options: Map[String, Any] = Map()
    ) {

        /** Adds a list of Channel Handlers to the pipeline */
        def add ( handler: (String, ChannelHandler)* ): Builder = new Builder(
            address, List(handler:_*).reverse ::: pipeline, options
        )

        /** Sets an option */
        def set ( option: (String, Any)* ): Builder = new Builder(
            address, pipeline, options ++ Map( option:_* )
        )

        /** Sets the keep alive option */
        def keepAlive: Builder = set("keepAlive" -> true)

        /** Sets the tcpNoDelay option */
        def tcpNoDelay: Builder = set("tcpNoDelay" -> true)

        /** Sets the connection timeout in milliseconds */
        def connectTimeout( milliseconds: Int ): Builder
            = set("connectTimeoutMillis" -> milliseconds)

        /** Uses the current config to open a connection */
        def connect: Future[Channel] = {
            val bootstrap = new ClientBootstrap( channelFactory )

            options.map( pair => bootstrap.setOption( pair._1, pair._2 ) )

            val resolvedPipe = Channels.pipeline
            bootstrap.setPipeline( resolvedPipe )
            pipeline.map( h => resolvedPipe.addFirst( h._1, h._2 ) )

            Netty.futureify( bootstrap.connect(address) )
        }

    }

    /** Initializes a connection to the given address */
    def open ( address: InetSocketAddress ): Builder = new Builder( address )

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


