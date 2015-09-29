package com.cloudera.sa.node360.flume;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.service.HBaseService;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ted.malaska on 6/9/15.
 */
public class EventInterceptor implements Interceptor {

  HBaseService hbaseService;

  @Override
  public void initialize() {
    Configuration config = HBaseConfiguration.create();
    config.addResource("/etc/hbase/conf/hbase-site.xml");
    //config.set(HConstants.ZOOKEEPER_QUORUM, "fce-4.vpc.cloudera.com,fce-3.vpc.cloudera.com,fce-2.vpc.cloudera.com");
    //config.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");
    try {
      hbaseService = new HBaseService(config);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Event intercept(Event event) {
    return event;
  }

  @Override
  public List<Event> intercept(List<Event> list) {

    List<EventPojo> eventList = new ArrayList<EventPojo>();

    for (Event event: list) {
      ByteArrayInputStream bos = new ByteArrayInputStream(event.getBody());
      ObjectInput input = null;
      try {
        input = new ObjectInputStream(bos);
        Object o = input.readObject();
        if (o instanceof EventPojo) {
          EventPojo eventPojo = (EventPojo) o;
          eventList.add(eventPojo);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    try {
      hbaseService.publishEvents(eventList);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return list;
  }
  @Override
  public void close() {
  }

  public static class Builder implements Interceptor.Builder
  {
    @Override
    public void configure(Context context) {
      // TODO Auto-generated method stub
    }

    @Override
    public Interceptor build() {
      return new EventInterceptor();
    }
  }
}


