import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import static tinyscalautils.java.Text.STANDARD_MODE.println;

class FutureTaskDemo {

  public static void main(String[] args) throws Exception {
    FutureTask<String> ftask = new FutureTask<>(() -> "foo"); // a task that produces "foo"
    println(ftask.isDone()); // the task is not done yet
    ftask.run();
    println(ftask.isDone()); // now the task is done
    println(ftask.get()); // and its result is "foo"

    ftask = new FutureTask<>(() -> {
      throw new NullPointerException(); // a task that fails
    });
    println(ftask.isDone()); // the task is not done yet
    ftask.run(); // does not throw
    println(ftask.isDone()); // now the task is done
    try {
      ftask.get(); // throws NullPointerException wrapped inside ExecutionException
    } catch (ExecutionException e) {
      println(e.getCause()); // this is the NullPointerException
    }
  }
}
