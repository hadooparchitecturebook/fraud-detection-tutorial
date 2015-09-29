package com.cloudera.sa.node360.service.utils;

import com.cloudera.sa.node360.constant.TopLevelConst;
import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.NetFlowPojo;
import com.cloudera.sa.node360.model.NotificationPojo;
import com.cloudera.sa.node360.model.SamplePojo;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Row;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ted.malaska on 6/13/15.
 */
public class GlobalStatsUtil {

  public static long totalEvents;
  public static ConcurrentHashMap<byte[], TotalWrapper> nodeTotals =
          new ConcurrentHashMap<byte[], TotalWrapper>();

  public static void processEvents(List<Row> increments, List<EventPojo> events) {
    for (EventPojo event : events) {

      //TODO check for time pass

      if (event.getType().equals(EventPojo.NETFLOW_TYPE)) {
        TotalWrapper totalWrapper = nodeTotals.get(convertIpToBytes(event.getNode()));
        for (NetFlowPojo netFlowPojo : event.getNetFlowPojoList()) {
          totalWrapper.totalOutputNetwork += netFlowPojo.getNumberOfBytes();
          totalEvents ++;
        }
      } else if (event.getType().equals(EventPojo.LINE_TYPE) &&
              event.getGroup().equals(TopLevelConst.DRIVES_LINE_CHART_DATA)) {
        TotalWrapper totalWrapper = nodeTotals.get(convertIpToBytes(event.getNode()));

        boolean updateMaxDiskSpace = totalWrapper.totalDiskSpace == 0;


        for (SamplePojo samplePojo : event.getSamples()) {
          totalWrapper.diskUsed += Long.parseLong(samplePojo.getValue());
          if (updateMaxDiskSpace) {
            totalWrapper.totalDiskSpace = Long.parseLong(samplePojo.getMax());
          }
        }
      }
    }
  }

  private static class TotalWrapper {
    long diskUsed = 0;
    long totalDiskSpace = 0;
    long totalOutputNetwork;
  }
  /*
    long totalDiskUsed = 0;
    long totalDiskCap = 0;
    long totalOutputNetwork;
    long maxOutputNetwork;
    long minOutputNetwork;
   */

  public static byte[] convertIpToBytes(String node) {
    String[] parts = StringUtils.split(node, ',');
    byte[] nodeBytes = new byte[4];
    nodeBytes[0] = Byte.parseByte(parts[0]);
    nodeBytes[1] = Byte.parseByte(parts[1]);
    nodeBytes[2] = Byte.parseByte(parts[2]);
    nodeBytes[3] = Byte.parseByte(parts[3]);

    return nodeBytes;
  }
}
