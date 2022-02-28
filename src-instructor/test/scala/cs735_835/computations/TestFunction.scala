package cs735_835.computations

import scala.util.{ Failure, Success, Try }
import tinyscalautils.assertions.{ require, requireState }

import java.util.concurrent.atomic.AtomicBoolean

class TestFunction[A, B](result: Try[B], autoFinish: Boolean)
    extends java.util.function.Function[A, B]:

   for (e <- result.failed) do require(e.isInstanceOf[RuntimeException])

   val task            = TestTask(result, autoFinish)
   var actualInput     = Option.empty[A]
   private val invoked = AtomicBoolean()

   def apply(x: A): B =
      requireState(!invoked.getAndSet(true), "function already invoked")
      actualInput = Some(x)
      try task.call()
      catch
         case _: InterruptedException => Thread.currentThread.interrupt()
         case e: Exception =>
            assert(e.isInstanceOf[RuntimeException], "functions cannot throw checked exceptions")
            throw e.asInstanceOf[RuntimeException]
      result.get

   def input: A =
      requireState(task.isStarted, "function not started")
      assert(actualInput.nonEmpty)
      actualInput.get

   def caller: Thread = task.caller

   def isStarted: Boolean = task.isStarted

   def isFinished: Boolean = task.isFinished

   def waitForStart(seconds: Double = 1.0): Boolean = task.waitForStart(seconds)

   def finish(): Unit = task.finish()
end TestFunction

class SuccessfulTestFunction[A, B](output: B, autoFinish: Boolean = false)
    extends TestFunction[A, B](Success(output), autoFinish)

class FailedTestFunction[A, B](exception: RuntimeException, autoFinish: Boolean = false)
    extends TestFunction[A, B](Failure(exception), autoFinish)
