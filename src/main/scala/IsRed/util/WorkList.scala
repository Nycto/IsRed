package com.roundeights.isred

import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.util.{Success, Failure}

/**
 * Executes a list of methods in the order they are queued
 */
class WorkList[T] ( implicit context: ExecutionContext ) {

    type WorkType = Function0[Future[T]]

    /** The list of work to run */
    private val work = new FutureQueue[WorkType]

    /** Recursively executes callbacks */
    private def run: Unit = {

        // @FIXME: Potential memory leak here. If the WorkList object is
        // ever garbage collected, there could still be a dangling reference
        // to the dequeued Future waiting to execute. It will be waiting a long
        // time.

        work.dequeue.onComplete {
            case Success(callback) => {
                callback().onComplete { case _ => run }
            }
            case Failure(_) => {
                // FIXME: Should we be doing anything with the error?
                run
            }
        }
    }

    // Start the execution loop
    run

    /** Queues the given function for execution */
    def flatMap ( callback: () => Future[T] ): Future[T] = {
        val output = Promise[T]

        work.enqueue(() => {
            val result = callback()
            output.completeWith( result )
            result
        })

        output.future
    }

    /** Queues a callback for execution */
    def map ( callback: () => T ): Future[T]
        = flatMap( () => Future { callback() } )

    /** Queues a  thunk for execution */
    def run ( thunk: => Future[T] ): Future[T] = flatMap( () => thunk )

    /** Queues a thunk for execution */
    def defer ( thunk: => T ): Future[T] = map( () => thunk )
}

