package com.adt.devblog.hazelcast.service;

import com.adt.devblog.hazelcast.configuration.HazelcastConfiguration;
import com.adt.devblog.hazelcast.task.FibonacciTask;
import com.adt.devblog.hazelcast.task.FibonacciTaskResult;
import com.adt.devblog.hazelcast.task.FibonacciTaskStatus;
import com.adt.devblog.hazelcast.exception.CancelledTaskException;
import com.hazelcast.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class DistributedFibonacciTaskService {
  private final HazelcastInstance hcInstance;

  @Autowired
  public DistributedFibonacciTaskService(HazelcastInstance hcInstance) {
    this.hcInstance = hcInstance;
    getFibonacciTaskMap().addEntryListener(new TaskMapListener(), true);
  }

  /**
   * Submits the given fibonacci task to the default executor service and adds an entry to the status map with status RUNNING.
   *
   * @param fibonacciTaskStatus The task status object carrying the information to submit a task.
   * @return The UUID of the submitted task.
   */
  public String submit(FibonacciTaskStatus fibonacciTaskStatus) {
    String taskUUID = UUID.randomUUID().toString();
    IExecutorService executorService = hcInstance.getExecutorService(HazelcastConfiguration.REST_EXECUTOR_SERVICE);

    // update task status object
    fibonacciTaskStatus.setStatus(FibonacciTaskStatus.STATUS.SUBMITTED);
    getFibonacciTaskMap().put(taskUUID, fibonacciTaskStatus);

    // submit the task for execution
    FibonacciTask fibonacciTask = new FibonacciTask(taskUUID, fibonacciTaskStatus.getN());
    executorService.submit(fibonacciTask, new ExecutionCallback<FibonacciTaskResult>() {
      @Override
      public void onResponse(FibonacciTaskResult response) {
        getFibonacciTaskResultMap().put(taskUUID, response);

        FibonacciTaskStatus fibonacciTaskStatus = getFibonacciTaskStatus(taskUUID);
        fibonacciTaskStatus.setStatus(FibonacciTaskStatus.STATUS.FINISHED);
        getFibonacciTaskMap().put(taskUUID, fibonacciTaskStatus);
      }

      @Override
      public void onFailure(Throwable t) {
        FibonacciTaskStatus fibonacciTaskStatus = getFibonacciTaskStatus(taskUUID);

        if (t instanceof CancelledTaskException) {
          getFibonacciTaskMap().remove(taskUUID);
          getFibonacciTaskResultMap().remove(taskUUID);
          return;
        }

        // set error state
        fibonacciTaskStatus.setStatus(FibonacciTaskStatus.STATUS.ERROR);
        fibonacciTaskStatus.setStatusMessage(t.getMessage());
        getFibonacciTaskMap().put(taskUUID, fibonacciTaskStatus);
      }
    });

    return taskUUID;
  }

  /**
   * Returns the fibonacci task status object for the given UUID.
   *
   * @param taskUUID The UUID of the map entry.
   * @return The fibonacci task status object, or {@code null}.
   */
  public FibonacciTaskStatus getFibonacciTaskStatus(String taskUUID) {
    if (null == taskUUID)
      return null;

    return getFibonacciTaskMap().get(taskUUID);
  }

  /**
   * Returns the fibonacci task result object for the given UUID.
   *
   * @param taskUUID The UUID of the map entry.
   * @return The fibonacci task result object, or {@code null}.
   */
  public FibonacciTaskResult getFibonacciTaskResult(String taskUUID) {

    if (null == taskUUID)
      return null;

    FibonacciTaskResult fibonacciTaskResult = getFibonacciTaskResultMap().get(taskUUID);

    if (null == fibonacciTaskResult)
      return null;

    return fibonacciTaskResult;
  }

  /**
   * Cancels fibonacci resource creation.
   *
   * @param taskUUID The UUID of the map entry.
   */
  public void cancelFibonacciTask(String taskUUID) {
    FibonacciTaskStatus fibonacciTaskStatus = getFibonacciTaskMap().get(taskUUID);

    if (null == fibonacciTaskStatus)
      return;

    // delete result and fibonacciTaskStatus if status equals ended state
    if (FibonacciTaskStatus.STATUS.FINISHED.equals(fibonacciTaskStatus.getStatus()) || FibonacciTaskStatus.STATUS.ERROR.equals(fibonacciTaskStatus.getStatus())) {
      getFibonacciTaskMap().remove(taskUUID);
      getFibonacciTaskResultMap().remove(taskUUID);
    } else {
      fibonacciTaskStatus.setStatus(FibonacciTaskStatus.STATUS.CANCELLED);
      getFibonacciTaskMap().put(taskUUID, fibonacciTaskStatus);
    }

  }

  private IMap<String, FibonacciTaskStatus> getFibonacciTaskMap() {
    return hcInstance.getMap(HazelcastConfiguration.TASK_MAP);
  }

  private IMap<String, FibonacciTaskResult> getFibonacciTaskResultMap() {
    return hcInstance.getMap(HazelcastConfiguration.TASK_RESULT_MAP);
  }

  /**
   * Returns a List with all existing fibonacci task status objects.
   *
   * @return List with all existing fibonacci task status objects.
   */
  public ArrayList<FibonacciTaskStatus> getFibonacciTaskList() {
    return new ArrayList<>(getFibonacciTaskMap().values());
  }
}