package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.funsuite.AnyFunSuite
import tinyscalautils.threads.joined

trait SampleTest4:
   self: AnyFunSuite =>

   test("Sample test 4") {
      val main = Thread.currentThread
      val task = SuccessfulTestTask("T")
      val f1   = SuccessfulTestFunction[String, Int](1)
      val f2   = SuccessfulTestFunction[String, Int](2)
      val f3   = SuccessfulTestFunction[String, Int](3, autoFinish = true)
      val f4   = SuccessfulTestFunction[String, Int](4, autoFinish = true)
      val comp = newComputation(task)

      val comp1 = comp.map(f1) // first continuation specified while the task is running
      assert(!f1.isStarted) // continuation not started yet
      task.finish()
      assert(f1.waitForStart())        // first continuation starts...
      assert(f1.caller eq task.caller) // in the computation thread...
      assert(f1.input == "T")          // with the output of the computation as its input
      assert(!comp1.isFinished)
      val comp2 = comp.map(f2) // second continuation added before the first one finished
      assert(!f2.isStarted)            // continuation not started yet
      f1.finish()                      // first continuation finishes
      assert(f2.waitForStart())        // second continuation starts...
      assert(f2.caller eq task.caller) // in the computation thread...
      assert(f2.input == "T")          // with the output of the computation as its input
      f2.finish()                      // second continuation finishes...
      val comp3 = comp.map(f3) // at the same time a third computation is specified
      assert(f3.waitForStart())                                 // third continuation starts...
      assert((f3.caller eq task.caller) || (f3.caller eq main)) // in an existing thread...
      assert(f3.input == "T")         // with the output of the computation as its input
      assert(comp1.get() == 1)        // output of first continuation
      assert(comp2.get() == 2)        // output of second continuation
      assert(comp3.get() == 3)        // output of third continuation
      assert(task.caller.joined(1.0)) // computation thread terminates
      val comp4 = comp.map(f4)
      println("test4: isDone:"+comp4.asInstanceOf[NewThreadComputation[Int]].runner.isDone)
      assert(comp4.isFinished)  // continuation is already terminated
      assert(f4.caller eq main) // it ran in the calling thread...
      assert(f4.input == "T")   // with the output of the computation as its input
      assert(comp4.get() == 4)  // output of fourth continuation
   }
