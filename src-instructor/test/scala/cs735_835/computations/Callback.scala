package cs735_835.computations

import java.util.concurrent.atomic.{ AtomicInteger, AtomicReference }

class Callback extends Runnable:
   private val calls        = AtomicInteger()
   private val callerThread = AtomicReference[Thread]()

   def run(): Unit =
      callerThread.set(Thread.currentThread)
      calls.incrementAndGet()

   def caller: Thread = callerThread.get

   def callCount: Int = calls.get
