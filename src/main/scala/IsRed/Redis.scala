package com.roundeights.isred

import scala.concurrent.{Future, ExecutionContext}
import org.jboss.netty.channel._
import scala.reflect.ClassTag

/**
 * An interface for constructing Redis commands
 */
class Redis
    ( private val engine: Engine )
    ( implicit context: ExecutionContext )
extends Iface with Hashes with Keys with Lists with Sets with Strings {

    /** Constructs a new redis interface */
    def this
        ( host: String, port: Int, maxConnect: Int = 5 )
        ( implicit context: ExecutionContext )
    = this ( new Engine(host, port, maxConnect) )

    /** Shuts down all the resources associated with this instace */
    def shutdown: Unit = engine.shutdown

    /** {@inheritDoc} */
    type AckResult = Future[Boolean]

    /** {@inheritDoc} */
    type IntResult = Future[Int]

    /** {@inheritDoc} */
    type FloatResult = Future[Double]

    /** {@inheritDoc} */
    type BoolResult = Future[Boolean]

    /** {@inheritDoc} */
    type BulkResult[A] = Future[A]

    /** {@inheritDoc} */
    type OptBulkResult[A] = Future[Option[A]]

    /** {@inheritDoc} */
    type BulkSetResult[A] = Future[Set[A]]

    /** {@inheritDoc} */
    type BulkSeqResult[A] = Future[Seq[A]]

    /** {@inheritDoc} */
    type BulkMapResult[A] = Future[Map[String, A]]

    /** {@inheritDoc} */
    type PopResult[A] = Future[(String, A)]

    /** {@inheritDoc} */
    type KeyListResult = Future[Seq[String]]

    /** {@inheritDoc} */
    type KeyResult = Future[String]

    /** {@inheritDoc} */
    type KeyTypeResult = Future[String]

    /** {@inheritDoc} */
    override def getAck( command: Command ): AckResult
        = engine.send( command ).map( _.asAck )

    /** {@inheritDoc} */
    override def getInt( command: Command ): IntResult
        = engine.send( command ).map( _.asInt )

    /** {@inheritDoc} */
    override def getFloat( command: Command ): FloatResult
        = engine.send( command ).map( _.asDouble )

    /** {@inheritDoc} */
    override def getBool( command: Command ): BoolResult
        = engine.send( command ).map( _.asBool )

    /** {@inheritDoc} */
    override def getBulk[A : Convert]( command: Command ): BulkResult[A]
        = engine.send( command ).map( implicitly[Reply => A] _ )

    /** {@inheritDoc} */
    override def getOptBulk[A : Convert](
        command: Command
    ): OptBulkResult[A] = engine.send( command ).map( _ match {
        case NullReply() => None
        case reply: Reply => Some[A]( reply )
    })

    /** {@inheritDoc} */
    override def getBulkSet[A : Convert](
        command: Command
    ): BulkSetResult[A] = engine.send( command ).map {
        _.asSeq.foldLeft( Set[A]() )(_ + _)
    }

    /** {@inheritDoc} */
    override def getBulkSeq[A : Convert](
        command: Command
    ): BulkSeqResult[A] = engine.send( command ).map {
        _.asSeq.map( implicitly[Reply => A] _ )
    }

    /** {@inheritDoc} */
    override def getBulkMap[A : Convert](
        command: Command
    ): BulkMapResult[A] = engine.send( command ).map {
        _.asSeq.grouped(2).foldLeft( Map[String, A]() ) {
            (accum, list) => list match {
                case Seq(key, value) => accum + (key.asString -> value)
                case _ => accum
            }
        }
    }

    /** {@inheritDoc} */
    override def getPop[A : Convert]( command: Command ): PopResult[A] = {
        engine.send( command ).map { _ match {
            case NullReply() => throw PopTimeout( command )
            case tuple => tuple.asSeq match {
                case Seq(list, value) => (list.asString, value)
                case _ => throw UnexpectedReply("List Pop Tuple", tuple)
            }
        }}
    }

    /** {@inheritDoc} */
    override def getKeyList( command: Command ): KeyListResult
        = getBulkSeq[String]( command )

    /** {@inheritDoc} */
    override def getKey( command: Command ): KeyResult
        = getBulk[String]( command )

    /** {@inheritDoc} */
    override def getKeyType( command: Command ): KeyTypeResult
        = getBulk[String]( command )

}

