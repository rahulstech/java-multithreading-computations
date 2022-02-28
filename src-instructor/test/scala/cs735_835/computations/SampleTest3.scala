package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite
import tinyscalautils.threads.joined
import tinyscalautils.threads.newThread
import tinyscalautils.timing.sleep

trait SampleTest3:
   self: AnyFunSuite =>

   test("Sample test 3") {
      val task               = SuccessfulTestTask("T")
      val callbackTask       = SuccessfulTestTask("C")
      val callback: Runnable = () => callbackTask.call() // a long running callback
      val comp               = newComputation(task, callback)

      task.finish()
      assert(callbackTask.waitForStart()) // callback starts after task finishes
      assert(!comp.isFinished)            // computation is unfinished while callback is running
      val t = newThread(start = true)(comp.get()) // a thread calls comp.get()
      sleep(0.5)
      assert(t.isAlive)     // the thread is stuck on get()
      callbackTask.finish() // callback finishes
      // both threads terminate
      assert(task.caller.joined(1.0))
      assert(t.joined(1.0))
   }
