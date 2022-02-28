package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.{ CancellationException, ExecutionException }
import tinyscalautils.threads.joined

trait SampleTest6:
   self: AnyFunSuite =>

   test("Sample test 6") {
      val ex   = Exception()                            // exception thrown by the task
      val task = FailedTestTask(ex)                     // a failing task
      val comp = newComputation(task)
      val f1   = SuccessfulTestFunction[String, Int](1) // first continuation
      val f2   = SuccessfulTestFunction[String, Int](2) // second continuation
      val callback1: Runnable = () => throw RuntimeException() // a failing callback
      val callback2           = Callback()                     // a second callback

      // registering callbacks
      comp.onComplete(callback1)
      comp.onComplete(callback2)
      // registering continuations
      val comp1 = comp.map(f1)
      val comp2 = comp.map(f2)
      task.finish() // the task fails
      // comp throws ExecutionException with task exception as its cause
      assert(intercept[ExecutionException](comp.get()).getCause eq ex)
      // comp1 and comp2 throw CancellationException
      assertThrows[CancellationException](comp1.get())
      assertThrows[CancellationException](comp2.get())
      assert(callback2.callCount == 1) // second callback was executed
      // all computations are finished
      assert(comp.isFinished)
      assert(comp1.isFinished)
      assert(comp2.isFinished)
      // the continuations were never called
      assert(!f1.isStarted)
      assert(!f2.isStarted)
      assert(task.caller.joined(1.0))
   }
