package com.adt.devblog.hazelcast.task;

import com.adt.devblog.hazelcast.configuration.HazelcastConfiguration;
import com.adt.devblog.hazelcast.exception.CancelledTaskException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A long running task to calculate fibonacci(n) recursively.
 */
public class FibonacciTask implements Callable<FibonacciTaskResult>, HazelcastInstanceAware, Serializable {

  private final Long n;
  private Long syncPoint;

  @Getter
  private HazelcastInstance hcInstance;

  @Setter
  @Getter
  private String taskUUID;

  public FibonacciTask(String taskUUID, Long n) {
    this.taskUUID = taskUUID;
    this.n = n;
  }

  @Override
  public void setHazelcastInstance(HazelcastInstance hcInstance) {
    this.hcInstance = hcInstance;
  }

  @Override
  public FibonacciTaskResult call() {

    syncPoint = System.currentTimeMillis();

    // check if task was cancelled before started
    if (FibonacciTaskStatus.STATUS.CANCELLED.equals(getJobStatus())) {
      throw new CancelledTaskException("FibonacciTaskStatus was cancelled before start.");
    }

    // update status with running state
    setJobStatus(FibonacciTaskStatus.STATUS.RUNNING);

    // start calculation
    FibonacciTaskResult fibonacciTaskResult = new FibonacciTaskResult();
    fibonacciTaskResult.setResult(calculate(n));

    return fibonacciTaskResult;
  }

  private Long calculate(Long n) {
    if (n <= 1L) {
      return n;
    } else {

      // since getJobStatus is very time consuming the status request is limited to "every 15 seconds"
      if ((System.currentTimeMillis() - syncPoint) >= 15000L) {
        syncPoint = System.currentTimeMillis();
        if (FibonacciTaskStatus.STATUS.CANCELLED.equals(getJobStatus())) {
          throw new CancelledTaskException("FibonacciTaskStatus was cancelled during execution.");
        }
      }

      return calculate(n - 1L) + calculate(n - 2L);
    }
  }

  public FibonacciTaskStatus.STATUS getJobStatus() {
    return ((FibonacciTaskStatus) hcInstance.getMap(HazelcastConfiguration.TASK_MAP).get(taskUUID)).getStatus();
  }

  public void setJobStatus(FibonacciTaskStatus.STATUS status) {
    FibonacciTaskStatus fibonacciTaskStatus = (FibonacciTaskStatus) hcInstance.getMap(HazelcastConfiguration.TASK_MAP).get(taskUUID);
    fibonacciTaskStatus.setStatus(status);
    hcInstance.getMap(HazelcastConfiguration.TASK_MAP).put(taskUUID, fibonacciTaskStatus);
  }

}
