package com.roundeights.isred

import org.specs2.mutable._

import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executor

class FutureQueueTest extends Specification {

    /** Blocks while waiting for the given future */
    def await[T] ( future: Future[T] ): T
        = Await.result( future, Duration(10, "second") )

    /** An execution context that runs in the calling thread */
    implicit val context = ExecutionContext.fromExecutor(new Executor {
        override def execute( command: Runnable ): Unit = command.run
    })

    "A FutureQueue" should {

        "Dequeue previously enqueued values" in {
            val q = FutureQueue[String]()
            q.enqueue("one").enqueue("two").enqueue("three")

            await( q.dequeue ) must_== "one"
            await( q.dequeue ) must_== "two"
            await( q.dequeue ) must_== "three"
        }

        "Dequeue values that haven't yet been enqueued" in {
            val q = FutureQueue[String]()
            val one = q.dequeue
            val two = q.dequeue
            val three = q.dequeue

            q.enqueue("one").enqueue("two").enqueue("three")

            await( one ) must_== "one"
            await( two ) must_== "two"
            await( three ) must_== "three"
        }

        "Transition between having waitiers and having values" in {
            val q = FutureQueue[String]()
            val one = q.dequeue
            val two = q.dequeue

            q.enqueue("one").enqueue("two")
            await( one ) must_== "one"
            await( two ) must_== "two"

            q.enqueue("three").enqueue("four")
            await( q.dequeue ) must_== "three"
            await( q.dequeue ) must_== "four"
        }

        "Transition between having values and having waiters" in {
            val q = FutureQueue[String]()

            q.enqueue("one").enqueue("two")
            await( q.dequeue ) must_== "one"
            await( q.dequeue ) must_== "two"

            val three = q.dequeue
            val four = q.dequeue
            q.enqueue("three").enqueue("four")
            await( three ) must_== "three"
            await( four ) must_== "four"
        }

    }

}


