package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite

import java.util
import tinyscalautils.threads.joined

trait SampleTest8:
   self: AnyFunSuite =>

   test("Sample test 8") {
      val task1    = SuccessfulTestTask("T1")
      val task2    = SuccessfulTestTask("T2")
      val callback = Callback()

      // computation created from a list of 2 tasks, with a callback
      val comp = newComputation(util.List.of(task1, task2), callback)
      // both tasks start and run in parallel
      assert(task1.waitForStart())
      assert(task2.waitForStart())
      assert(task1.isStarted && task2.isStarted)
      assert(task1.caller ne task2.caller)
      task2.finish()                  // one task finishes
      assert(!task1.isFinished)       // the other task is still running
      assert(!comp.isFinished)        // and therefore the computation is not finished
      assert(callback.callCount == 0) // and the callback is not run
      task1.finish()                  // the other task finishes
      val results = comp.get() // the computation finishes and get() unblocks
      assert(comp.isFinished)
      assert(callback.callCount == 1) // the callback has been run
      // the computation produced a list of results: [T1,T2]
      assert(results.get(0) == "T1")
      assert(results.get(1) == "T2")
      // the callback was run in the computation thread, which is one of the task running threads
      assert((callback.caller eq task1.caller) || (callback.caller eq task2.caller))
      // both created threads properly terminate
      assert(task1.caller.joined(1.0))
      assert(task2.caller.joined(1.0))
   }
