package com.cloudera.sa.node360.nodepuller.listener;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.SamplePojo;
import com.cloudera.sa.node360.service.QueueService;

import java.util.List;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class InternalQueueListener implements EventListener {

  @Override
  public void publishEvent(EventPojo event) {
    while (QueueService.internalEventQueue.size() > 1000000) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    QueueService.internalEventQueue.add(event);
  }
  /*
  @Override
  public void publishEvent(String node, long timestamp, List<SamplePojo> samples) {

  }

  @Override
  public void publishEvent(String node, long timestamp, String group, List<SamplePojo> samples) {
    QueueService.internalEventQueue.add(new EventPojo(node, timestamp, group, samples));
  }

  @Override
  public void publishEvent(String node, long timestamp, String group, String meta, String newFile) {
    QueueService.internalEventQueue.add(new EventPojo(node, timestamp, group, meta, newFile));
  }
  */
}
