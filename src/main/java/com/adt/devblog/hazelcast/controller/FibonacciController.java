package com.adt.devblog.hazelcast.controller;

import com.adt.devblog.hazelcast.service.DistributedFibonacciTaskService;
import com.adt.devblog.hazelcast.task.FibonacciTaskResult;
import com.adt.devblog.hazelcast.task.FibonacciTaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * The task resource controller.
 */
@RestController
public class FibonacciController {

  private final DistributedFibonacciTaskService distributedFibonacciTaskService;

  @Autowired
  public FibonacciController(DistributedFibonacciTaskService distributedFibonacciTaskService) {
    this.distributedFibonacciTaskService = distributedFibonacciTaskService;
  }

  /**
   * Endpoint to request the status of all existing fibonacci tasks.
   *
   * @return HttpStatus.ACCEPTED on success.
   */
  @RequestMapping(value = "/fibonacci/", method = RequestMethod.GET)
  public HttpEntity<List<FibonacciTaskStatus>> getFibonacciTaskList() {
    List<FibonacciTaskStatus> fibonacciTaskStatusList = distributedFibonacciTaskService.getFibonacciTaskList();

    return new ResponseEntity<>(fibonacciTaskStatusList, HttpStatus.OK);
  }

  /**
   * Endpoint to request the fibonacci resource creation.
   *
   * @param fibonacciTaskStatus The attributes for the fibonacci resource creation.
   * @param b Builder for UriComponents, @see org.springframework.web.util.UriComponentsBuilder
   * @return HttpStatus.ACCEPTED on success.
   */
  @RequestMapping(value = "/fibonacci/", method = RequestMethod.POST)
  public HttpEntity<Void> submitJob(@RequestBody FibonacciTaskStatus fibonacciTaskStatus, UriComponentsBuilder b) {
    String taskId = distributedFibonacciTaskService.submit(fibonacciTaskStatus);

    UriComponents uriComponents = b.path("/fibonacci/{jobId}").buildAndExpand(taskId);

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
  }

  /**
   * Endpoint to request the current status of a fibonacci resource creation.
   *
   * @param taskId The id of the fibonacci resource to check the status for.
   * @param b Builder for UriComponents, @see org.springframework.web.util.UriComponentsBuilder
   * @return HttpStatus.NOT_FOUND if the id is not known, HttpStatus.OK if the resource creation is not yet finished
   * or HttpStatus.SEE_OTHER if the resource has been created.
   */
  @RequestMapping(value = "/fibonacci/{taskId}", method = RequestMethod.GET)
  public HttpEntity<FibonacciTaskStatus> getJob(@PathVariable("taskId") String taskId, UriComponentsBuilder b) {
    FibonacciTaskStatus fibonacciTaskStatus = distributedFibonacciTaskService.getFibonacciTaskStatus(taskId);

    if (null == fibonacciTaskStatus) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (FibonacciTaskStatus.STATUS.FINISHED.equals(fibonacciTaskStatus.getStatus()) || FibonacciTaskStatus.STATUS.CANCELLED.equals(fibonacciTaskStatus.getStatus())) {
      UriComponents uriComponents = b.path("/fibonacci/{taskId}/result").buildAndExpand(taskId);

      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(uriComponents.toUri());
      return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    } else {
      return new ResponseEntity<>(fibonacciTaskStatus, HttpStatus.OK);
    }
  }

  /**
   * Endpoint to delete a fibonacci task resource including the result.
   *
   * @param taskId The id of the task resource to be deleted.
   * @return HttpStatus.NOT_FOUND if the id is not known, HttpStatus.OK if the resource was deleted.
   */
  @RequestMapping(value = "/fibonacci/{taskId}", method = RequestMethod.DELETE)
  public HttpEntity<FibonacciTaskStatus> deleteJob(@PathVariable("taskId") String taskId) {
    FibonacciTaskStatus fibonacciTaskStatus = distributedFibonacciTaskService.getFibonacciTaskStatus(taskId);

    if (null == fibonacciTaskStatus) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    distributedFibonacciTaskService.cancelFibonacciTask(taskId);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Endpoint to request the fibonacci task result.
   *
   * @param taskId The id of the fibonacci task resource to request the result for.
   * @return HttpStatus.NOT_FOUND if the id is not known or the resource is not yet finished,
   * HttpStatus.OK including the result if the resource has been created.
   */
  @RequestMapping(value = "/fibonacci/{taskId}/result", method = RequestMethod.GET)
  public HttpEntity<FibonacciTaskResult> getJobResult(@PathVariable("taskId") String taskId) {
    FibonacciTaskResult fibonacciTaskResult = distributedFibonacciTaskService.getFibonacciTaskResult(taskId);

    if (null == fibonacciTaskResult) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(fibonacciTaskResult, HttpStatus.OK);
  }
}
