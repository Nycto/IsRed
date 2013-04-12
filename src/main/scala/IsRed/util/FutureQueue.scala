package com.roundeights.isred

import scala.collection.immutable.Queue
import scala.concurrent.{Future, Promise}
import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicReference

/** FutureQueue Companion */
private[isred] object FutureQueue {
    /** Builds a new future queue */
    def apply[A](): FutureQueue[A] = new FutureQueue[A]()
}

/**
 * A mutable queue that returns a future when popping a value
 *
 * Based on the implementation found here:
 * https://groups.google.com/forum/#!msg/scala-user/lyoAdNs3E1o/zenchh75NSkJ
 */
private[isred] class FutureQueue[A] {

    /**
     * There are three states this queue can be in
     * 1) An excess of enqueued values, which is represented by a
     *      Queue in the `Right` slot
     * 2) An excess of promises waiting for a value to be enqueued, which
     *      is represented by a Queue of Promises in the `Left` slot
     * 3) No waiters, no values: which is an empty queue in the `Right` slot
     */
    private val data: AtomicReference[Either[Queue[Promise[A]],Queue[A]]]
        = new AtomicReference( Right( Queue() ) )

    /**
     * Adds a value to the queue
     * @return Returns a self reference
     */
    @tailrec final def enqueue ( value: A ): FutureQueue[A] = data.get match {

        // The queue doesn't have any waiters
        case ref@Right(values) => {
            if ( data.compareAndSet(ref, Right(values.enqueue(value)) ) )
                this
            else
                enqueue( value )
        }

        // The queue has a single waiter
        case ref@Left( Queue(waiter) ) => {
            if ( data.compareAndSet(ref, Right( Queue() )) ) {
                waiter.success( value )
                this
            } else {
                enqueue( value )
            }
        }

        // The queue has multiple waiters
        case ref@Left( waiters: Queue[Promise[A]] ) => {
            val (waiter, tail) = waiters.dequeue
            if ( data.compareAndSet(ref, Left(tail) ) ) {
                waiter.success( value )
                this
            } else {
                enqueue( value )
            }
        }
    }

    /** Removes a value from the queue */
    @tailrec final def dequeue: Future[A] = data.get match {

        // There is no value in the queue, but there is a list of waiters
        case ref@Left( waiters: Queue[Promise[A]] ) => {
            val promise = Promise[A]()
            if ( data.compareAndSet(ref, Left(waiters.enqueue(promise))) )
                promise.future
            else
                dequeue
        }

        // There are no values in the queue, and no waiters
        case ref@Right( Queue() ) => {
            val promise = Promise[A]()
            if ( data.compareAndSet(ref, Left(Queue(promise))) )
                promise.future
            else
                dequeue
        }

        // There is at least one value in the queue
        case ref@Right( values: Queue[A] ) => {
            val (value, tail) = values.dequeue
            if ( data.compareAndSet(ref, Right(tail)) )
                Future.successful( value )
            else
                dequeue
        }
    }

}

