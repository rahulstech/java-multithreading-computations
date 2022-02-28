package cs735_835.computations;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static tinyscalautils.java.Assertions.TODO;

// The class is private, but its name cannot be changed.
class NewThreadComputation<A> implements Computation<A> {

  MyThread thread;
  MyCallable<A> callable;
  MyFutureTask<A> runner;

  NewThreadComputation() {}

  NewThreadComputation(MyThread thread, Callable<A> task, Runnable... callbacks) {
    init(false, thread, task, callbacks);
  }

  NewThreadComputation(Callable<A> task, Runnable... callbacks) {
    this(new MyThread(),task,callbacks);
  }

  NewThreadComputation<A> initFinished(Callable<A> task, Runnable... callbacks) {
    return init(true,null,task,callbacks);
  }

  NewThreadComputation<A> init(boolean isFinished, MyThread worker, Callable<A> task, Runnable... callbacks) {
    callable = new MyCallable<>(task,callbacks);
    runner = new MyFutureTask<>(callable);
    if (isFinished) {
      runner.run();
    }
    else {
      if (null == worker) this.thread = new MyThread();
      else this.thread = worker;
      this.thread.enqueue(runner);
    }
    return this;
  }

  public static <T> Computation<T> newComputation(Callable<? extends T> task, Runnable... callbacks) {
    return new NewThreadComputation<T>((Callable<T>) task,callbacks);
  }

  public static <T> Computation<List<T>> newComputation(
      List<? extends Callable<? extends T>> tasks,
      Runnable... callbacks
  ) {
    if (tasks.isEmpty()) {
      return new NewThreadComputation<List<T>>().initFinished(
              () -> Collections.emptyList(),callbacks);
    }
    final NewThreadComputation<List<T>> mainComputation = new NewThreadComputation<>();
    Callable<List<T>> mainTask = () -> {
        Runnable callbacksCallback = new CallbacksCallback(tasks.size(), callbacks);

        ArrayList<NewThreadComputation<T>> computations = new ArrayList<>();
        for (Callable<?> task : tasks) {
          NewThreadComputation<T> computation
                  = new NewThreadComputation<>((Callable<T>) task,callbacksCallback);
          computations.add(computation);
        }
        ArrayList<T> results = new ArrayList<>();
        for (NewThreadComputation<T> computation : computations)
          results.add(computation.get());
        return results;
    };

    mainComputation.init(false,null,mainTask);
    return mainComputation;
  }

  public boolean isFinished() {
    return runner.isDone();
  }

  public A get() throws InterruptedException, ExecutionException, CancellationException {
    return runner.get();
  }

  public void onComplete(Runnable callback) {
    if (runner.isDone()) {
      callback.run();
    }
    else {
      callable.enqueue(callback);
    }
  }

  public <B> Computation<B> map(Function<? super A, B> f) {
    if (runner.isCancelled()) {
      return new NewThreadComputation<B>().initFinished(()->f.apply(null));
    }
    if (isFinished()) {
      return new NewThreadComputation<B>().initFinished(()->null);
    }
    return new NewThreadComputation<B>(thread,() -> {
      A input = get();
      return f.apply(input);
    });
  }

  public <B> Computation<B> mapParallel(Function<? super A, B> f) {
    if (runner.isCancelled()) {
      return new NewThreadComputation<B>().initFinished(()->f.apply(null));
    }
    if (isFinished()) {
      return new NewThreadComputation<B>().initFinished(()->null);
    }
    return new NewThreadComputation<B>(new MyThread(),() -> {
      A input = get();
      return f.apply(input);
    });
  }
}

class CallbacksCallback implements Runnable {

  final CountDownLatch latch;
  final Queue<Runnable> qCallbacks = new LinkedList<>();

  CallbacksCallback(int count, Runnable... callbacks) {
    latch = new CountDownLatch(count);
    if (null != callbacks) {
      for (Runnable cb : callbacks)
        qCallbacks.add(cb);
    }
  }

  @Override
  public void run() {
    synchronized (latch) {
      latch.countDown();
    }

    try {latch.await();}
    catch (InterruptedException ignore) {}

    synchronized (qCallbacks) {
      while (!qCallbacks.isEmpty()) {
        Runnable cb = qCallbacks.remove();
        try {cb.run();}
        catch (Exception ignore) {}
      }
    }
  }
}

class MyCallable<O> implements Callable<O> {

  Callable<O> task;
  Lock qLock = new ReentrantLock();
  Queue<Runnable> qCallback = new LinkedList<>();

  MyCallable(Callable<O> task, Runnable... callbacks) {
    this.task = task;
    if (null != callbacks) {
      for (Runnable cb : callbacks) {
        enqueue(cb);
      }
    }
  }

  public Callable<O> getTask() {
    return task;
  }

  void enqueue(Runnable callback) {
    if (null != callback) {
      qLock.lock();
      qCallback.add(callback);
      qLock.unlock();
    }
  }

  @Override
  public O call() throws Exception {
    final O result = task.call();
    executeCallbacks();
    return result;
  }

  public void executeCallbacks() {
    while (true) {
      qLock.lock();
      if (qCallback.isEmpty()) {
        qLock.unlock();
        break;
      }
      final Runnable callback = qCallback.remove();
      qLock.unlock();
      try {callback.run();}catch (Exception ignore) {}
    }
  }
}

class MyFutureTask<O> extends FutureTask<O> {

  MyCallable<O> callable;

  MyFutureTask(MyCallable<O> callable) {
    super(callable);
    this.callable = callable;
  }

  public MyCallable<O> getCallable() {
    return callable;
  }
}

class MyThread extends Thread {

  CountDownLatch latch = new CountDownLatch(1);
  Lock qLock = new ReentrantLock();
  Queue<MyFutureTask<?>> qTasks = new LinkedList<>();

  MyThread() {
    super.start();
  }

  void enqueue(MyFutureTask<?> task) {
    qLock.lock();
    qTasks.add(task);
    qLock.unlock();
    latch.countDown();
  }

  @Override
  public void run() {
    try {latch.await();}
    catch (InterruptedException ex) {}

    while (true) {
      qLock.lock();
      if (qTasks.isEmpty()) {
        qLock.unlock();
        break;
      }
      final MyFutureTask<?> task = qTasks.remove();
      qLock.unlock();

      task.run();

      try {
        task.get();
      }
      catch (InterruptedException|CancellationException|ExecutionException ex) {
        task.getCallable().executeCallbacks();

        qLock.lock();
        while (!qTasks.isEmpty()) {
          MyFutureTask<?> t = qTasks.remove();
          t.cancel(true);
          t.getCallable().executeCallbacks();
        }
        qLock.unlock();
      }
    }
  }
}
