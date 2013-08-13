package com.roundeights.isred

import org.specs2.mutable._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicInteger

class WorkListTest extends Specification {

    /** Blocks while waiting for the given future */
    def await[T] ( future: Future[T] ): T
        = Await.result( future, Duration(10, "second") )

    "A WorkList" should {

        "Execute a list of tasks" in {
            val work = new WorkList[String]

            val one = work.run( Future.successful("one") )
            val two = work.run( Future.successful("two") )
            val three = work.run( Future.successful("three") )

            await( one ) must_== "one"
            await( two ) must_== "two"
            await( three ) must_== "three"
        }

        "Run tasks in order" in {
            val work = new WorkList[Int]

            val incrementor = new AtomicInteger(0)

            val first = Promise[Int]

            val one = work.run( first.future )
            val two = work.defer( incrementor.incrementAndGet )
            val three = work.defer( incrementor.incrementAndGet )

            first.success( incrementor.incrementAndGet )

            await( one ) must_== 1
            await( two ) must_== 2
            await( three ) must_== 3
        }

        "Continue executing when a task fails" in {
            val err = new Exception("Expected error")

            val work = new WorkList[String]

            val one = work.run( Future.successful("one") )
            val two = work.run( Future.failed(err) )
            val three = work.run( Future.successful("three") )

            await( one ) must_== "one"
            await( two.failed ) must_== err
            await( three ) must_== "three"
        }
    }
}

