package com.cloudera.sa.node360.service.utils;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.NetFlowPojo;
import com.cloudera.sa.node360.model.NotificationPojo;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ted.malaska on 6/13/15.
 */
public class SecurityRulesWrapper {

  public static ConcurrentHashMap<ByteArrayWrapper, ConcurrentHashMap<ByteArrayWrapper, HistoricalTimes>> scrNodeToDestNodeMap =
          new ConcurrentHashMap<ByteArrayWrapper, ConcurrentHashMap<ByteArrayWrapper, HistoricalTimes>>();

  public static List<NotificationPojo> processEvents(List<EventPojo> events) {
    List<NotificationPojo> notificationPojos = new ArrayList<NotificationPojo>();
    for (EventPojo event: events) {
      if (event.getType().equals(EventPojo.NETFLOW_TYPE)) {

        ByteArrayWrapper nodeBytes = convertIpToBytes(event.getNode());

        ConcurrentHashMap<ByteArrayWrapper, HistoricalTimes> byteSet =
                scrNodeToDestNodeMap.get(nodeBytes);

        if (byteSet == null) {
          byteSet = new ConcurrentHashMap<ByteArrayWrapper, HistoricalTimes>();
          scrNodeToDestNodeMap.put(nodeBytes, byteSet);
        }

        for (NetFlowPojo netFlowPojo : event.getNetFlowPojoList()) {

          final ByteArrayWrapper byteArrayWrapper = convertIpToBytes(netFlowPojo.getDestinationAddress());

          HistoricalTimes historicalTimes = byteSet.get(byteArrayWrapper);

          if (historicalTimes == null) {

            String noteString = "Behavior anomaly - " + netFlowPojo.getSourceAddress() + " has new connection to new host " + netFlowPojo.getDestinationAddress();

            notificationPojos.add(new NotificationPojo(event.getTimestamp(),
                    noteString, System.currentTimeMillis() - event.getTimestamp()));

            byteSet.put(byteArrayWrapper, new HistoricalTimes(event.getTimestamp(), 0));
          } else {
            if (historicalTimes.applyNewTimeStamp(event.getTimestamp())) {
              String noteString = "Potential APT - Beaconing { scrNode:" + event.getNode() + ", destNode:"
                      + netFlowPojo.getDestinationAddress() + ", intervalTime:" + historicalTimes.getLastInterval() + "}";

              notificationPojos.add(new NotificationPojo(event.getTimestamp(),
                      noteString, System.currentTimeMillis() - event.getTimestamp()));
            }
          }
        }
      }
    }
    return notificationPojos;
  }

  public static ByteArrayWrapper convertIpToBytes(String node) {
    String[] parts = StringUtils.split(node, '.');
    byte[] nodeBytes = new byte[4];
    nodeBytes[0] = Byte.parseByte(parts[0]);
    nodeBytes[1] = Byte.parseByte(parts[1]);
    nodeBytes[2] = Byte.parseByte(parts[2]);
    nodeBytes[3] = Byte.parseByte(parts[3]);

    return new ByteArrayWrapper(nodeBytes);
  }

  public static final class ByteArrayWrapper
  {
    private final byte[] data;

    public ByteArrayWrapper(byte[] data)
    {
      if (data == null)
      {
        throw new NullPointerException();
      }
      this.data = data;
    }

    @Override
    public boolean equals(Object other)
    {
      if (!(other instanceof ByteArrayWrapper))
      {
        return false;
      }
      return Arrays.equals(data, ((ByteArrayWrapper) other).data);
    }

    @Override
    public int hashCode()
    {
      return Arrays.hashCode(data);
    }
  }

  public static final class HistoricalTimes {
    long lastTimeStamp;
    long lastInterval;

    public HistoricalTimes(long lastTimeStamp, long lastInterval) {
      this.lastTimeStamp = lastTimeStamp;
      this.lastInterval = lastInterval;
    }

    public boolean applyNewTimeStamp(long newTimeStamp) {
      long newDiff = newTimeStamp - lastTimeStamp;
      if (newDiff > 2) {
        boolean match = (newDiff == lastInterval);
        lastTimeStamp = newTimeStamp;
        lastInterval = newDiff;
        return match;
      } else {
        return false;
      }
    }

    public long getLastTimeStamp() {
      return lastTimeStamp;
    }

    public void setLastTimeStamp(long lastTimeStamp) {
      this.lastTimeStamp = lastTimeStamp;
    }

    public long getLastInterval() {
      return lastInterval;
    }

    public void setLastInterval(long lastInterval) {
      this.lastInterval = lastInterval;
    }
  }
}
