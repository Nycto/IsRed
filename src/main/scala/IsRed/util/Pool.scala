package com.roundeights.isred

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.annotation.tailrec

/** Companion */
object Pool {

    /** Builds a new pool */
    def apply[A]
        ( max: Int, builder: () => A )
        ( implicit context: ExecutionContext )
    : Pool[A]
        = new Pool[A]( max, builder )( context )

}

/** A pool of values */
class Pool[A] (
    private val max: Int,
    private val builder: () => A
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

        /** Releases this value back to the pool */
        def release: Unit = {
            available.getAndIncrement
            queue.enqueue( value )
        }

    }

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
                context.execute( new Runnable { override def run = {
                    try {
                        promise.success( new Value( builder() ) )
                    } catch {
                        case err: Throwable => {
                            created.getAndDecrement
                            promise.failure( err )
                        }
                    }
                }})
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
                callback( value.value )
            } finally {
                value.release
            }
        }}
    }

}

