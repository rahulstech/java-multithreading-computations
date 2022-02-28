package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite

import java.util.Collections

trait SampleTest7:
   self: AnyFunSuite =>

   test("Sample test 7") {
      val main     = Thread.currentThread
      val callback = Callback()

      // computation created from an empty list of tasks
      val comp = newComputation(Collections.emptyList(), callback)
      assert(comp.isFinished)         // computation is returned finished
      assert(callback.caller == main) // callback is run in current thread
      assert(comp.get().isEmpty)      // computation result is an empty list
   }
