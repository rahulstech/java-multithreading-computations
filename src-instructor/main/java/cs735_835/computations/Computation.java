package cs735_835.computations;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * A background computation.  A computation typically runs into its own thread, but the same thread
 * can run multiple computations in sequence.  Using a computation to run a task instead of a thread
 * offers multiple benefits:
 * <ul>
 * <li><em>Callbacks</em> can be registered that are guaranteed to run at the end of the
 * computation.</li>
 * <li><em>Continuations</em> can be created that process the result of a computation into
 * another computation.</li>
 * <li>Continuation computations can be made to run in the same thread as the finishing
 * computation (for efficiency) or in another thread (for parallelism).</li>
 * <li>A thread can wait for the result of a computation using a blocking method.</li>
 * <li><em>Failures</em> are handled by computations in a systematic way (see handout for details).
 * </li>
 * </ul>
 */
public interface Computation<A> {

  /**
   * Whether a computation is completed.  After this method returns true, it is guaranteed that:
   * <ul>
   * <li>all the callbacks previously registered have been executed</li>
   * <li>the {@code get} method will not block</li>
   * </ul>
   * Note that the thread that ran the computation might still be alive, running continuations.
   *
   * @return true iff the computation has finished, including callbacks (but not continuations)
   */
  boolean isFinished();

  /**
   * The result of the computation.  This method may block the calling thread if the computation has
   * yet to terminate.
   *
   * @throws ExecutionException    if the computation failed.  Any exception thrown by the task can
   *                               be retrieved as the cause of this exception.
   * @throws CancellationException if this computation never ran.  This happens to the continuations
   *                               of a failed or cancelled computation.
   */
  A get() throws InterruptedException, ExecutionException, CancellationException;

  /**
   * Registers a callback.  If the computation's thread is still running the task or other
   * callbacks, it will be used to run this callback as well.  If the thread is terminated or has
   * been reused to run continuations, this callback is executed in the calling thread.
   */
  void onComplete(Runnable callback);

  /**
   * Creates a continuation.  The function is fed the result of this computation as its input. If
   * the computation's thread is still running the task or callbacks or other continuations, it will
   * be used to run this continuation as well.  If the thread is terminated, the specified function
   * is executed in the calling thread.
   * <p>
   * If this computation fails or is cancelled, no input is available and the computation being
   * returned is cancelled without running.
   *
   * @param f the function to execute.
   */
  <B> Computation<B> map(Function<? super A, B> f);

  /**
   * Creates a continuation.  The function is fed the result of this computation as its input, and
   * runs in a new thread.
   * <p>
   * If this computation fails or is cancelled, no input is available and the computation being
   * returned is cancelled without running.
   *
   * @param f the function to execute.
   */
  <B> Computation<B> mapParallel(Function<? super A, B> f);

  /**
   * Creates a new computation in a new thread.  The thread is started.
   * <p>The instance returned by this method is <em>thread-safe</em>.</p>
   *
   * @param task      the task to execute in the computation.
   * @param callbacks callbacks to execute at the end of the computation.  These are guaranteed to
   *                  run in the same thread as the task (this is not always guaranteed for
   *                  callbacks added after the computation has started).
   * @param <T>       the type of the task.
   * @return a running computation, in a freshly created thread.
   */
  static <T> Computation<T> newComputation(Callable<? extends T> task, Runnable... callbacks) {
    return NewThreadComputation.newComputation(task, callbacks);
  }

  /**
   * Creates a new computation that executes a list of tasks in parallel.  This method creates
   * exactly as many threads as there are tasks in the list.  If the list is empty, no thread is
   * created, and a computation is returned already finished, with all its callbacks run.
   * Otherwise, one of the task running threads is also the computation thread, and will run the
   * specified callbacks after all tasks have completed.
   * <p>The instance returned by this method is <em>thread-safe</em>.</p>
   *
   * @param tasks     a list of tasks to run in parallel.
   * @param callbacks callbacks to execute at the end of the computation, after all tasks have
   *                  finished.
   * @param <T>       the type of the tasks.
   * @return a computation that uses as many threads as there are tasks. The outcome of this
   * computation is a list of all the task results, in the same order as the tasks.
   */
  static <T> Computation<List<T>> newComputation(List<? extends Callable<? extends T>> tasks,
                                                 Runnable... callbacks) {
    return NewThreadComputation.newComputation(tasks, callbacks);
  }
}
