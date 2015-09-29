package com.cloudera.sa.node360.flume;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.NetFlowPojo;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.sink.hdfs.SequenceFileSerializer;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ted.malaska on 6/12/15.
 */
public class NetFlowEventSerializer implements SequenceFileSerializer {

  Logger logger = Logger.getLogger(NetFlowEventSerializer.class.getName());

  Text text = new Text();

  @Override
  public Class<NullWritable> getKeyClass() {
    return NullWritable.class;
  }

  @Override
  public Class<Text> getValueClass() {
    return Text.class;
  }

  @Override
  public Iterable<Record> serialize(Event event) {

    //logger.error("event.getBody():" + event.getBody().length);
    //logger.error("eventHeader:");
    //for (Map.Entry<String, String> stringStringEntry : event.getHeaders().entrySet()) {
    //  logger.error(" - " + stringStringEntry.getKey() + "-" + stringStringEntry.getValue());
    //}


    ByteArrayInputStream bos = new ByteArrayInputStream(event.getBody());
    ObjectInput input = null;
    try {
      input = new ObjectInputStream(bos);
      Object o = input.readObject();

      //logger.error("o type:" + o.toString());

      if (o instanceof EventPojo) {

        EventPojo eventPojo = (EventPojo)o;

        //logger.error("in :" + o.toString());

        final List<NetFlowPojo> netFlowPojoList = eventPojo.getNetFlowPojoList();

        //logger.error("eventPojo.getType :" + eventPojo.getType());
        //logger.error("eventPojo.getNode :" + eventPojo.getNode());

        //logger.error("netFlowPojoList :" + netFlowPojoList);

        List<Record> list = new ArrayList<Record>();

        if (netFlowPojoList != null) {
          //logger.error("netFlowPojoList.size :" + netFlowPojoList.size());

          for (NetFlowPojo netFlow: netFlowPojoList) {

            text.set(eventPojo.getTimestamp() + "," + netFlow.getSourceAddress() + "," + netFlow.getSourcePort() + "," +
                    netFlow.getProtocal() + "," + netFlow.getNumberOfBytes() + "," +
                    netFlow.getDestinationAddress() + "," + netFlow.getDestinationPort());

            list.add(new Record(NullWritable.get(), text));
          }

          //logger.error("list :" + list.size());

        }
        return list;

      } else {
        throw new RuntimeException("Record is not of type " + EventPojo.class + " but instead a class of " + o.getClass());
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class Builder implements SequenceFileSerializer.Builder {

    @Override
    public SequenceFileSerializer build(Context context) {

      return new NetFlowEventSerializer();
    }

  }
}
