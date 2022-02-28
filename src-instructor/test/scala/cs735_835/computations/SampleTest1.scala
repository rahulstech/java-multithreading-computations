package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite
import tinyscalautils.threads.joined

trait SampleTest1:
   self: AnyFunSuite =>

   test("Sample test 1") {
      val main = Thread.currentThread
      val task = SuccessfulTestTask("T") // the underlying task
      val comp = newComputation(task)

      assert(task.waitForStart())     // the task starts to run
      assert(!comp.isFinished)        // the computation is not finished
      task.finish()                   // finish the task
      assert(comp.get() == "T")       // get blocks the thread, then produces the result "T"
      assert(comp.isFinished)         // once get returns, the computation is finished
      assert(task.caller ne main)     // the task ran in a separate thread
      assert(task.caller.joined(1.0)) // this thread now terminates
   }
