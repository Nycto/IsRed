package com.roundeights.isred

import org.specs2.mutable._

import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

class PoolTest extends Specification {

    /** Blocks while waiting for the given future */
    def await[T] ( future: Future[T] ): T
        = Await.result( future, Duration(3, "second") )

    /** An execution context that runs in the calling thread */
    implicit val context = ExecutionContext.fromExecutor(new Executor {
        override def execute( command: Runnable ): Unit = command.run
    })

    "A Pool" should {

        "Recycle values once a previous block is done using them" in {
            val counter = new AtomicInteger(0)
            val pool = Pool(1,
                () => Future.successful(counter.incrementAndGet)
            )

            await( pool( value => {
                value must_== 1
                "One"
            })) must_== "One"

            await( pool( value => {
                value must_== 1
                "Two"
            })) must_== "Two"
        }

        "Build up to the max when values are borrowed" in {
            val counter = new AtomicInteger(0)
            val pool = Pool(2,
                () => Future.successful(counter.incrementAndGet)
            )

            val one = await( pool.borrow )
            one.value must_== 1

            val two = await( pool.borrow )
            two.value must_== 2

            val three = pool.borrow
            val four = pool.borrow

            one.release
            two.release

            await( three ).value must_== 1
            await( four ).value must_== 2
        }

        "Attempt to build more values when the builder throws" in {
            val error = new Exception("Expected error")
            val counter = new AtomicInteger(0)
            val pool = Pool(2, () => {
                val value = counter.incrementAndGet
                if ( value == 2 ) throw error
                else Future.successful(value)
            })

            await( pool.borrow ).value must_== 1
            await( pool( value => failure ).failed ) must_== error
            await( pool.borrow ).value must_== 3
        }

        "Attempt to build more values when the built future fails" in {
            val error = new Exception("Expected error")
            val counter = new AtomicInteger(0)
            val pool = Pool(2, () => {
                val value = counter.incrementAndGet
                if ( value == 2 ) Future.failed(error)
                else Future.successful(value)
            })

            await( pool.borrow ).value must_== 1
            await( pool( value => failure ).failed ) must_== error
            await( pool.borrow ).value must_== 3
        }

        "Retire a value when a callback throws" in {
            val counter = new AtomicInteger(0)
            val pool = Pool(1,
                () => Future.successful(counter.incrementAndGet)
            )

            val error = new Exception("Expected error")
            await( pool(value => {
                value must_== 1
                throw error
            }).failed ) must_== error

            await( pool( value => {
                value must_== 2
                "Result"
            })) must_== "Result"
        }

        "Build more values when a value is retired" in {
            val counter = new AtomicInteger(0)
            val pool = Pool(1,
                () => Future.successful(counter.incrementAndGet)
            )

            val first = await( pool.borrow )
            first.value must_== 1
            first.retire

            await( pool.borrow ).value must_== 2
        }

    }

}

