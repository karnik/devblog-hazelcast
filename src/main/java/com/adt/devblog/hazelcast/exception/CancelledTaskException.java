package com.adt.devblog.hazelcast.exception;

public class CancelledTaskException extends RuntimeException {
  public CancelledTaskException(String message) {
    super(message);
  }
}
