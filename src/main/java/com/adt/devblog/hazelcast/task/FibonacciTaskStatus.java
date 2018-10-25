package com.adt.devblog.hazelcast.task;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class FibonacciTaskStatus implements Serializable {

  @NotNull
  private Long n;
  private STATUS status;
  private String statusMessage;

  public enum STATUS {
    SUBMITTED,
    RUNNING,
    FINISHED,
    CANCELLED,
    ERROR
  }

}
