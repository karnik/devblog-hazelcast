package com.adt.devblog.hazelcast.task;

import lombok.Data;

import java.io.Serializable;

@Data
public class FibonacciTaskResult implements Serializable {
  private Long result;
}
