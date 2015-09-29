package com.cloudera.sa.node360.nodepuller.listener;

import com.cloudera.sa.node360.kafka.EventKafkaProducer;
import com.cloudera.sa.node360.model.EventPojo;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/9/15.
 */
public class KafkaListener implements EventListener {

  EventKafkaProducer newFlowProducer;
  EventKafkaProducer nodeStatusProducer;

  public KafkaListener (String netFlowTopic, String dataStatusTopic, String broker) {
    newFlowProducer = new EventKafkaProducer(netFlowTopic);
    newFlowProducer.setConfigure(broker);
    newFlowProducer.start();

    nodeStatusProducer = new EventKafkaProducer(dataStatusTopic);
    nodeStatusProducer.setConfigure(broker);
    nodeStatusProducer.start();
  }

  @Override
  public void publishEvent(EventPojo event) throws ExecutionException, InterruptedException, IOException {
    if (event.getType().equals(EventPojo.NETFLOW_TYPE)) {
      newFlowProducer.produce(event);
    } else {
      nodeStatusProducer.produce(event);
    }
  }
}
