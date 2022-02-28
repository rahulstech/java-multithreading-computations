package cs735_835.computations

import cs735_835.computations.Computation.newComputation
import org.scalatest.events.TestSucceeded
import org.scalatest.funsuite.AnyFunSuite
import tinyscalautils.threads.joined

trait SampleTest9:
   self: AnyFunSuite =>

   test("Sample test 9") {
      val task               = SuccessfulTestTask("T")
      val callbackTask       = SuccessfulTestTask("C")
      val callback: Runnable = () => callbackTask.call()
      val f                  = SuccessfulTestFunction[String, Int](0)
      val comp               = newComputation(task, callback)

      task.finish()
      assert(callbackTask.waitForStart())
      // callback is running, nothing is blocking
      comp.isFinished
      comp.map(f)
      comp.mapParallel(SuccessfulTestFunction[String, Int](1, autoFinish = true))
      comp.onComplete(Callback())

      callbackTask.finish()
      assert(f.waitForStart())
      // continuation is running, nothing is blocking
      comp.isFinished
      comp.map(SuccessfulTestFunction[String, Int](2, autoFinish = true))
      comp.mapParallel(SuccessfulTestFunction[String, Int](3, autoFinish = true))
      comp.onComplete(Callback())

      f.finish()
      TestSucceeded
   }
