package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite
import tinyscalautils.threads.joined

trait SampleTest2:
   self: AnyFunSuite =>

   test("Sample test 2") {
      val main                                       = Thread.currentThread
      val callback1, callback2, callback3, callback4 = Callback()
      val task                                       = SuccessfulTestTask("T")

      // first callback specified at construction time
      val comp = newComputation(task, callback1)
      comp.onComplete(callback2) // callback added while the task is running
      assert(!comp.isFinished)
      task.finish()
      comp.onComplete(callback3) // racy callback as the task finishes
      comp.get()
      // get has returned; all the callbacks have run exactly once
      assert(callback1.callCount == 1)
      assert(callback2.callCount == 1)
      assert(callback3.callCount == 1)
      // callbacks 1 and 2 run by the computation thread
      assert(callback1.caller eq task.caller)
      assert(callback2.caller eq task.caller)
      // callback3 run by either thread
      assert((callback3.caller eq task.caller) || (callback3.caller eq main))
      assert(task.caller.joined(1.0)) // computation thread is terminated
      comp.onComplete(callback4)      // run in calling thread
      assert(callback4.callCount == 1)
      assert(callback4.caller == main)
   }
