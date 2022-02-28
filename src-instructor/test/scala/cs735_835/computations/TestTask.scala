package cs735_835.computations

import net.jcip.annotations.ThreadSafe

import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.{ Callable, CountDownLatch }
import scala.util.{ Failure, Success, Try }
import tinyscalautils.assertions.requireState

import java.util.concurrent.atomic.AtomicBoolean

@ThreadSafe
class TestTask[B](result: Try[B], autoFinish: Boolean) extends Callable[B]:

   private var callerThread = Option.empty[Thread]
   private val started      = CountDownLatch(1)
   private val finished     = CountDownLatch(1)
   private val called       = AtomicBoolean()

   def call(): B =
      requireState(!called.getAndSet(true), "task already called")
      callerThread = Some(Thread.currentThread)
      started.countDown()
      if autoFinish then finish()
      finished.await()
      result.get

   def caller: Thread =
      requireState(isStarted, "task not started")
      assert(callerThread.nonEmpty)
      callerThread.get

   def isStarted: Boolean = started.await(0, NANOSECONDS)

   def isFinished: Boolean = finished.await(0, NANOSECONDS)

   def waitForStart(seconds: Double = 1.0): Boolean =
      started.await((seconds * 1E9).round, NANOSECONDS)

   def finish(): Unit = finished.countDown()
end TestTask

@ThreadSafe
class SuccessfulTestTask[B](value: B, autoFinish: Boolean = false)
    extends TestTask[B](Success(value), autoFinish)

@ThreadSafe
class FailedTestTask[B](exception: Exception = new Exception, autoFinish: Boolean = false)
    extends TestTask[B](Failure(exception), autoFinish)
