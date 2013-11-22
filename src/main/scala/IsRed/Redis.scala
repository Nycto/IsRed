package com.roundeights.isred

import scala.concurrent.{Future, ExecutionContext}
import org.jboss.netty.channel._
import scala.reflect.ClassTag

/**
 * An interface for constructing Redis commands
 */
class Redis
    ( private val engine: Sendable )
    ( implicit context: ExecutionContext )
extends Iface with Hashes with Keys with Lists
with Sets with Strings with Connection with Scripting {

    /** Constructs a new redis interface */
    def this(
        host: String,
        port: Int = 6379,
        maxConnect: Int = 5,
        connectTimeout: Int = 2000,
        onConnect: Function1[Redis,Future[_]] = (_) => Future.successful(Unit)
    )( implicit context: ExecutionContext ) = this( new Engine(
        host, port, maxConnect, connectTimeout,
        (channel) => onConnect( new Redis(channel) )
    ) )

    /** Shuts down all the resources associated with this instace */
    def shutdown: Unit = engine.close

    /** {@inheritDoc} */
    type AnyResult[A] = Future[A]

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
    type TtlResult = Future[Option[Int]]

    /** {@inheritDoc} */
    type PopResult[A] = Future[(String, A)]

    /** {@inheritDoc} */
    type KeyListResult = Future[Seq[Key]]

    /** {@inheritDoc} */
    type KeyResult = Future[Key]

    /** {@inheritDoc} */
    type KeyTypeResult = Future[KeyType.Type]

    /** {@inheritDoc} */
    override private[isred] def getAny[A: Convert](
        command: Command
    ): AnyResult[A] = engine.send( command ).map( implicitly[Reply => A] _ )

    /** {@inheritDoc} */
    override private[isred] def getAck( command: Command ): AckResult
        = engine.send( command ).map( _.asAck )

    /** {@inheritDoc} */
    override private[isred] def getInt( command: Command ): IntResult
        = engine.send( command ).map( _.asInt )

    /** {@inheritDoc} */
    override private[isred] def getFloat( command: Command ): FloatResult
        = engine.send( command ).map( _.asDouble )

    /** {@inheritDoc} */
    override private[isred] def getBool( command: Command ): BoolResult
        = engine.send( command ).map( _.asBool )

    /** {@inheritDoc} */
    override private[isred] def getBulk[A : Convert](
        command: Command
    ): BulkResult[A]
        = engine.send( command ).map( implicitly[Reply => A] _ )

    /** {@inheritDoc} */
    override private[isred] def getOptBulk[A : Convert](
        command: Command
    ): OptBulkResult[A] = engine.send( command ).map( _ match {
        case NullReply() => None
        case reply: Reply => Some[A]( reply )
    })

    /** {@inheritDoc} */
    override private[isred] def getBulkSet[A : Convert](
        command: Command
    ): BulkSetResult[A] = engine.send( command ).map {
        _.asSeq.foldLeft( Set[A]() )(_ + _)
    }

    /** {@inheritDoc} */
    override private[isred] def getBulkSeq[A : Convert](
        command: Command
    ): BulkSeqResult[A] = engine.send( command ).map {
        _.asSeq.map( implicitly[Reply => A] _ )
    }

    /** {@inheritDoc} */
    override private[isred] def getBulkMap[A : Convert](
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
    override private[isred] def getTtl( command: Command ): TtlResult
        = getInt( command ).map { ttl => if ( ttl < 0 ) None else Some(ttl) }

    /** {@inheritDoc} */
    override private[isred] def getPop[A : Convert](
        command: Command
    ): PopResult[A] = {
        engine.send( command ).map { _ match {
            case NullReply() => throw PopTimeout( command )
            case tuple => tuple.asSeq match {
                case Seq(list, value) => (list.asString, value)
                case _ => throw UnexpectedReply("List Pop Tuple", tuple)
            }
        }}
    }

    /** {@inheritDoc} */
    override private[isred] def getKeyList( command: Command ): KeyListResult
        = getBulkSeq[Key]( command )

    /** {@inheritDoc} */
    override private[isred] def getKey( command: Command ): KeyResult
        = getBulk[Key]( command )

    /** {@inheritDoc} */
    override private[isred] def getKeyType( command: Command ): KeyTypeResult
        = getBulk[Reply]( command ).map { _.asKeyType }

}

