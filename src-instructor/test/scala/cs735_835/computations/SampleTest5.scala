package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable
import tinyscalautils.threads.joined

trait SampleTest5:
   self: AnyFunSuite =>

   test("Sample test 5") {
      val main    = Thread.currentThread
      val threads = mutable.Set.empty[Thread]
      val task    = SuccessfulTestTask("T")
      val f1      = SuccessfulTestFunction[String, Int](1)
      val f2      = SuccessfulTestFunction[String, Int](2)
      val f3      = SuccessfulTestFunction[String, Int](3)
      val comp    = newComputation(task)

      val comp1 = comp.mapParallel(f1) // first continuation specified while the task is running
      assert(!f1.isStarted) // continuation not started yet
      task.finish()
      assert(f1.waitForStart()) // first continuation starts...
      threads += task.caller
      assert(threads.add(f1.caller))  // in a new thread...
      assert(f1.input == "T")         // with the output of the computation as its input
      assert(task.caller.joined(1.0)) // computation thread terminates
      assert(!comp1.isFinished)
      val comp2 = comp.mapParallel(f2) // second continuation added before the first one finished
      assert(f2.waitForStart())            // it starts running immediately...
      assert(threads.add(f2.caller))       // in a new thread...
      assert(f2.input == "T")              // with the output of the computation as its input
      assert(f1.isStarted && f2.isStarted) // both continuations run in parallel
      f1.finish()
      f2.finish()
      // the first two continuations finish...
      // all threads now done...
      assert(f1.caller.joined(1.0))
      assert(f2.caller.joined(1.0))
      val comp3 = comp.mapParallel(f3) // before a third computation is specified
      assert(f3.waitForStart())      // third continuation starts...
      assert(threads.add(f3.caller)) // in a new thread...
      assert(f3.input == "T")        // with the output of the computation as its input
      f3.finish()
      assert(comp1.get() == 1)        // output of first continuation
      assert(comp2.get() == 2)        // output of second continuation
      assert(comp3.get() == 3)        // output of third continuation
      assert(f3.caller.joined(1.0))   // last thread terminates
      assert(threads.size == 4)       // four threads were used...
      assert(!threads.contains(main)) // none of them the calling thread
   }
