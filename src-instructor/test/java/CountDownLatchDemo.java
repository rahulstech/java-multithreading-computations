import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static tinyscalautils.java.Text.TIME_MODE.println;

class CountDownLatchDemo {

  public static void main(String[] args) throws Exception {
    var timer = Executors.newSingleThreadScheduledExecutor();

    CountDownLatch latch = new CountDownLatch(1); // a one-shot count down latch
    println(latch.getCount()); // the count is 1
    timer.schedule(latch::countDown, 10, SECONDS); // open the latch after 10 seconds
    println("waiting");
    latch.await(); // block for 10 seconds
    println(latch.getCount()); // the count is now 0
    latch.await(); // the latch does not block anymore
    println("done");

    timer.shutdown();
  }
}
