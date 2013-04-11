package com.roundeights.isred

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.annotation.tailrec

/** Companion */
object Pool {

    /** Builds a new pool */
    def apply[A]
        ( max: Int, builder: () => Future[A] )
        ( implicit context: ExecutionContext )
    : Pool[A]
        = new Pool[A]( max, builder )( context )
}

/** A pool of values */
class Pool[A] (
    private val max: Int,
    private val builder: () => Future[A]
)(
    implicit context: ExecutionContext
) {

    assert( max > 0 )

    /** The number of values that have been created */
    private val created: AtomicInteger = new AtomicInteger(0)

    /** The number of values that could be handed out */
    private val available: AtomicInteger = new AtomicInteger(0)

    /** The list of values */
    private val queue = FutureQueue[A]()

    /** A value borrowed from the pool */
    class Value private[Pool] ( val value: A ) {

        /** Whether this value has been released or retired */
        private val returned = new AtomicBoolean(false)

        /** Releases this value back to the pool */
        def release: Unit = if ( returned.compareAndSet(false, true) ) {
            available.getAndIncrement
            queue.enqueue( value )
        }

        /** Retires this value from service */
        def retire: Unit = if ( returned.compareAndSet(false, true) ) {
            created.getAndDecrement
        }
    }

    /** Builds a new pool value */
    private def buildInto( into: Promise[Value] ): Unit = {
        try {
            val built = builder()
            into.completeWith( built.map {
                value => new Value( value )
            })
            built.onFailure {
                case _ => created.getAndDecrement
            }
        } catch {
            case err: Throwable => {
                created.getAndDecrement
                into.failure( err )
            }
        }
    }

    /** Executes a block asynchronously */
    private def async ( block: => Unit )
        = context.execute( new Runnable { override def run = block } )

    /** Borrows a value from the pool */
    @tailrec final def borrow: Future[Value] = {
        val avail = available.get
        val creat = created.get

        if ( avail > 0 || creat == max ) {
            if ( available.compareAndSet(avail, avail - 1) )
                queue.dequeue.map { value => new Value(value) }
            else
                borrow
        }
        else {
            if ( created.compareAndSet(creat, creat + 1) ) {
                val promise = Promise[Value]()
                async { buildInto( promise ) }
                promise.future
            }
            else {
                borrow
            }
        }
    }

    /**
     * Executes a callback with a value from the queue and releases
     * the resource once its done
     */
    def apply[B] ( callback: A => B ): Future[B] = {
        borrow.map { value => {
            try {
                val result = callback( value.value )
                value.release
                result
            } catch {
                case err: Throwable => {
                    value.retire
                    throw err
                }
            }
        }}
    }

}

