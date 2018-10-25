package com.adt.devblog.hazelcast.service;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskMapListener
    implements EntryAddedListener<String, String>, EntryRemovedListener<String, String>, EntryUpdatedListener<String, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskMapListener.class);

  @Override
  public void entryAdded(EntryEvent<String, String> event) {
    LOGGER.info("FibonacciTaskStatus with ID {} added by {}", event.getKey(), event.getMember());
  }

  @Override
  public void entryRemoved(EntryEvent<String, String> event) {
    LOGGER.info("FibonacciTaskStatus with ID {} removed by {}. Value was: {}", event.getKey(), event.getMember(), event.getOldValue());
  }

  @Override
  public void entryUpdated(EntryEvent<String, String> event) {
    LOGGER.info("FibonacciTaskStatus with ID {} updated by {}. New value: {}, Old value: {}", event.getKey(), event.getMember(), event.getValue(), event.getOldValue());
  }
}
