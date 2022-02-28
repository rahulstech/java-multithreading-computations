package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite
import tinyscalautils.threads.joined
trait SampleTest10:
   self: AnyFunSuite =>

   test("Sample test 10") { // callback from callback
      val task                = SuccessfulTestTask(42)
      val comp                = newComputation(task)
      val callback2           = Callback()
      val callback1: Runnable = () => comp.onComplete(callback2) // callback1 registers callback2

      comp.onComplete(callback1) // main thread registers callback1
      assert(task.waitForStart())
      task.finish()
      assert(task.caller.joined(1.0))
      assert(callback2.callCount == 1)        // both callbacks ran
      assert(callback2.caller eq task.caller) // callback2 ran in computation thread
   }
