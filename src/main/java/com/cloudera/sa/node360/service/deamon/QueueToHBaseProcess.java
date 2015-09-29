package com.cloudera.sa.node360.service.deamon;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.service.HBaseService;
import com.cloudera.sa.node360.service.QueueService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class QueueToHBaseProcess {

  long sleepTime = 1;
  boolean running;
  HBaseService hbaseService;

  public QueueToHBaseProcess(HBaseService hbaseService) {
    this.hbaseService = hbaseService;
  }

  public QueueToHBaseProcess(HBaseService hbaseService, long sleepTime) {
    this.sleepTime = sleepTime;
    this.hbaseService = hbaseService;
  }

  public void start() {
    running = true;
    Thread t = new Thread(new PullingRunnable());
    t.start();
  }

  public void stop() {
    running = false;
  }

  private class PullingRunnable implements Runnable {

    @Override
    public void run() {
      int batchSize = 0;
      List<EventPojo> eventList = new ArrayList<EventPojo>();
      while (running) {
        EventPojo event;
        while (batchSize++ < 1000 && (event = QueueService.internalEventQueue.poll()) != null) {
          eventList.add(event);
        }
        try {
          hbaseService.publishEvents(eventList);
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException(t);
        }

        if (batchSize == 1) {
          try {
            Thread.sleep(2);
          } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
        if (batchSize < 1000) {
          try {
            Thread.sleep(sleepTime);
          } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
        batchSize = 0;
        eventList.clear();
      }
    }
  }
}
