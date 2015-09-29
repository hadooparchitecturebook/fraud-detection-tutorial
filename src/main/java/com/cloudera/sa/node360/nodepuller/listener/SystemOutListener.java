package com.cloudera.sa.node360.nodepuller.listener;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.NetFlowPojo;
import com.cloudera.sa.node360.model.SamplePojo;
import com.cloudera.sa.node360.service.QueueService;

import java.util.List;

/**
 * Created by ted.malaska on 6/3/15.
 */
public class SystemOutListener implements EventListener {

  @Override
  public void publishEvent(EventPojo event) {
    System.out.println("node:" + event.getNode() + ", timestamp:" + event.getTimestamp() + ", group:" + event.getGroup());
    if (event.getSamples() != null) {
      for (SamplePojo pojo : event.getSamples()) {
        System.out.println("  -" + pojo);
      }
    } else if (event.getNetFlowPojoList() != null) {
      for (NetFlowPojo netFlowPojo: event.getNetFlowPojoList()) {
        System.out.println(netFlowPojo);
      }
    } else {
      System.out.println("  - meta: " + event.getMeta());
      System.out.println("  - newFile: " + event.getNewFile());
    }
  }
  /*
  @Override
  public void publishEvent(String node, long timestamp, List<SamplePojo> samples) {
    System.out.println("node:" + node + ", timestamp:" + timestamp + ", group:" + "null");
    for (SamplePojo pojo: samples) {
      System.out.println("  -" + pojo);
    }
  }

  @Override
  public void publishEvent(String node, long timestamp, String group, List<SamplePojo> samples) {
    System.out.println("node:" + node + ", timestamp:" + timestamp + ", group:" + group);
    for (SamplePojo pojo: samples) {
      System.out.println("  -" + pojo);
    }
  }

  @Override
  public void publishEvent(String node, long timestamp, String group, String meta, String newFile) {
    System.out.println("node:" + node + ", timestamp:" + timestamp + ", group:" + group);
    System.out.println("  - meta: " + meta);
    System.out.println("  - newFile: " + newFile);


  }
  */
}
