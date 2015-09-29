package com.cloudera.sa.node360.service;

import com.cloudera.sa.node360.model.EventPojo;

import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueService {

  static public ConcurrentLinkedQueue<EventPojo> internalEventQueue = new ConcurrentLinkedQueue<EventPojo>();

}
