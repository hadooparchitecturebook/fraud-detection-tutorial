package com.cloudera.sa.node360.service;

import com.cloudera.sa.node360.constant.HBaseConst;
import com.cloudera.sa.node360.model.*;
import com.cloudera.sa.node360.service.utils.HBaseScanThreadUtil;
import com.cloudera.sa.node360.service.utils.SecurityRulesWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.source.kafka.KafkaSource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class HBaseService {

  static Connection connection;

  public HBaseService() {

  }

  public HBaseService(Configuration config) throws IOException {
    if (connection == null) {
      connection = ConnectionFactory.createConnection(config);
    }
  }

  public void addNode(NodePojo node) throws IOException {
    Table nodeTable = connection.getTable(HBaseConst.NODE_LIST_TABLE);

    Put put = new Put(Bytes.toBytes(node.getIpAddress()));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Nm"), Bytes.toBytes(node.getName()));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Zip"), Bytes.toBytes(node.getZipCode()));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Grp"), Bytes.toBytes(node.getGroup()));

    nodeTable.put(put);

    nodeTable.close();
  }

  public void addNodes(ArrayList<NodePojo> nodes) throws IOException {
    Table nodeTable = connection.getTable(HBaseConst.NODE_LIST_TABLE);

    ArrayList<Put> putList = new ArrayList<Put>();

    for (NodePojo node: nodes) {
      Put put = new Put(Bytes.toBytes(node.getIpAddress()));
      put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Nm"), Bytes.toBytes(node.getName()));
      put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Zip"), Bytes.toBytes(node.getZipCode()));
      put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Grp"), Bytes.toBytes(node.getGroup()));
      putList.add(put);
    }
    nodeTable.put(putList);

    nodeTable.close();
  }

  public NodePojo getNode(String ipAddress) throws IOException {
    Table nodeTable = connection.getTable(HBaseConst.NODE_LIST_TABLE);

    Get get = new Get(Bytes.toBytes(ipAddress));

    final Result result = nodeTable.get(get);

    NodePojo pojo = new NodePojo(ipAddress,
            Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Nm"))),
            Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Zip"))),
            Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Grp"))));

    nodeTable.close();

    return pojo;
  }

  public NodeStatusPojo getNodeStatus(String ipAddress) throws Exception {



    Table nodeTable = connection.getTable(HBaseConst.NODE_LIST_TABLE);

    Get get = new Get(Bytes.toBytes(ipAddress));

    final Result nodeResult = nodeTable.get(get);

    NodePojo nodePojo = new NodePojo(ipAddress,
            Bytes.toString(nodeResult.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Nm"))),
            Bytes.toString(nodeResult.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Zip"))),
            Bytes.toString(nodeResult.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Grp"))));

    nodeTable.close();

    List<byte[]> rowKeys = new ArrayList<byte[]>();
    rowKeys.add(Bytes.toBytes(ipAddress + "|" + "cpu" ));
    rowKeys.add(Bytes.toBytes(ipAddress + "|" + "memory" ));
    rowKeys.add(Bytes.toBytes(ipAddress + "|" + "swap" ));
    rowKeys.add(Bytes.toBytes(ipAddress + "|" + "drive" ));

    List<Result> results = HBaseScanThreadUtil.getNextResult(rowKeys, connection, HBaseConst.DETAILED_LINE_GRAPH_TABLE);

    Map<String, Map<String, SamplePojo>> groupSampleMap = new HashMap<String, Map<String, SamplePojo>>();

    for (Result result: results) {
      if (result != null) {
        String rowKey = Bytes.toString(result.getRow());

        String group = rowKey.substring(rowKey.indexOf('|') + 1, rowKey.lastIndexOf('|'));

        System.out.println("rowKey:" + rowKey + ",group:" + group);

        Map<String, SamplePojo> sampleMap = groupSampleMap.get(group);
        if (sampleMap == null) {
          sampleMap = new HashMap<String, SamplePojo>();
          groupSampleMap.put(group, sampleMap);
        }

        List<SamplePojo> sampleList = SamplePojo.readListOfSamplePojos(Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.BASE_COLUMN_QUALIFIER)));

        for (SamplePojo sample : sampleList) {
          sampleMap.put(sample.getKey(), sample);
        }
      } else {
        System.out.println("Bad we got a null");
      }

    }

    return new NodeStatusPojo(nodePojo, groupSampleMap);
  }

  public List<NodePojo> getAutoCompleteNodeList(String ipAddress, int maxResults) throws IOException {
    final List<NodePojo> results = new ArrayList<NodePojo>();

    Table nodeTable = connection.getTable(HBaseConst.NODE_LIST_TABLE);

    Scan scan = new Scan();
    scan.setStartRow(Bytes.toBytes(ipAddress));
    scan.setStopRow(Bytes.toBytes(ipAddress + "_"));
    scan.setCaching(10);

    final ResultScanner scanner = nodeTable.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    int counter = 0;
    while (counter++ < maxResults && iterator.hasNext()) {
      final Result result = iterator.next();

      String rowKey = Bytes.toString(result.getRow());

      results.add(new NodePojo(rowKey,
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Nm"))),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Zip"))),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Grp"))))
      );
    }

    nodeTable.close();

    return results;
  }

  public List<NodePojo> getFullNodeList() throws IOException {
    final List<NodePojo> results = new ArrayList<NodePojo>();

    Table nodeTable = connection.getTable(HBaseConst.NODE_LIST_TABLE);

    Scan scan = new Scan();
    scan.setCaching(100);

    final ResultScanner scanner = nodeTable.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    while (iterator.hasNext()) {
      final Result result = iterator.next();
      results.add(new NodePojo(Bytes.toString(result.getRow()),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Nm"))),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Zip"))),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes("Grp"))))
      );
    }

    nodeTable.close();

    return results;
  }


  private long lastRulesUpdate = 0;
  private List<RulePojo> ruleList = new ArrayList<RulePojo>();
  private long debugCounter = 0;

  public void publishEvents(List<EventPojo> events) throws IOException, InterruptedException {

    List<NotificationPojo> notificationPojos = new ArrayList<NotificationPojo>();

    KafkaSource k;

    //TODO Remove this into different thread
    synchronized (ruleList) {
      if (System.currentTimeMillis() - lastRulesUpdate > 10000) {
        ruleList = this.getAllRules();
        lastRulesUpdate = System.currentTimeMillis();
      }
    }

    Table detailedLineGraphTable = connection.getTable(HBaseConst.DETAILED_LINE_GRAPH_TABLE);

    final BufferedMutator detailedLineGraphBufferMutator = connection.getBufferedMutator(HBaseConst.DETAILED_LINE_GRAPH_TABLE);
    final BufferedMutator fileBufferedMutator = connection.getBufferedMutator(HBaseConst.FILE_CHANGE_TABLE);

    List<Row> increments = new ArrayList<Row>();

    if (events.size() > 0 && debugCounter++ >= 500) {
      debugCounter = 0;
      notificationPojos.add(new NotificationPojo(System.currentTimeMillis(),
              "Flume Heartbeat: ruleList:" + ruleList.size() +
                      ", events:" + events.size() +
                      ", eventType:" + events.get(0).getType() +
                      ", eventTimeStamp" + events.get(0).getTimestamp() +
                      ", currentTimeStamp" + System.currentTimeMillis(),
              System.currentTimeMillis() - events.get(0).getTimestamp()));
    }


    notificationPojos.addAll(SecurityRulesWrapper.processEvents(events));

    for (EventPojo event: events) {
      if (event.getType().equals(EventPojo.NETFLOW_TYPE)) {
        for (NetFlowPojo netFlowPojo : event.getNetFlowPojoList()) {

          for (RulePojo rulePojo : ruleList) {
            NotificationPojo notificationPojo = rulePojo.runRule(event.getNode(), event.getTimestamp(), event.getGroup(), netFlowPojo);
            if (notificationPojo != null) {
              notificationPojos.add(notificationPojo);
            }
          }

        }
      }
    }

    for (EventPojo event: events) {
      if (event.getType().equals(EventPojo.LINE_TYPE)) {
        putSampleLineChartEvent(detailedLineGraphBufferMutator, event.getNode(),
                event.getTimestamp(), event.getGroup(), event.getSamples());
      } else if (event.getType().equals(EventPojo.NETFLOW_TYPE)) {
        for (NetFlowPojo netFlowPojo : event.getNetFlowPojoList()) {
          //increments.add();
          createIncrementNetFlowDataEvent(detailedLineGraphBufferMutator, event.getNode(),
                  event.getTimestamp(), event.getGroup(), netFlowPojo);
        }
      } else {
        putFileEvent(fileBufferedMutator, event.getNode(),
                event.getTimestamp(), event.getGroup(),
                event.getMeta(), event.getNewFile());
      }
    }
    this.putNotifications(notificationPojos);

    detailedLineGraphBufferMutator.flush();
    fileBufferedMutator.flush();

    detailedLineGraphTable.batch(increments);

    detailedLineGraphTable.close();
    //fileChangeTable.close();
    detailedLineGraphBufferMutator.close();
    fileBufferedMutator.close();
  }



  private void createIncrementNetFlowDataEvent(BufferedMutator detailedListGraphBufferMutator, String node,
                                   long timestamp, String group, NetFlowPojo netFlowPojo) throws IOException {
    byte[] value = Bytes.toBytes(netFlowPojo.toString());
    //Increment increment = new Increment(Bytes.toBytes(generateRowKey(node, group, timestamp)));
    //increment.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes(netFlowPojo.generateColumn()), netFlowPojo.getNumberOfBytes());

    Put put = new Put(Bytes.toBytes(generateRowKey(node, group, timestamp)));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes(netFlowPojo.generateColumn()), Bytes.toBytes(Integer.toString(netFlowPojo.getNumberOfBytes())));

    detailedListGraphBufferMutator.mutate(put);


    //return increment;
  }

  /*
  public void publishEvent(String node, long timestamp, String group, List<SamplePojo> samples) throws IOException {
    Table table = connection.getTable(HBaseConst.DETAILED_LINE_GRAPH_TABLE);
    putSampleLineChartEvent(table, node, timestamp, group, samples);
    table.close();
  }
  */

  private void putSampleLineChartEvent(BufferedMutator detailedLineGraphBufferMutator, String node, long timestamp, String group, List<SamplePojo> samples) throws IOException {
    byte[] value = Bytes.toBytes(SamplePojo.toString(samples));
    Put put = new Put(Bytes.toBytes(generateRowKey(node, group, timestamp)));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.BASE_COLUMN_QUALIFIER, value);

    detailedLineGraphBufferMutator.mutate(put);
  }

  /*
  public void publishEvent(String node, long timestamp, String group, String meta, String newFile) throws IOException {
    Table table = connection.getTable(HBaseConst.FILE_CHANGE_TABLE);
    putFileEvent(table, node, timestamp, group, meta, newFile);
    table.close();
  }
  */

  private void putFileEvent(BufferedMutator fileBufferedMutator, String node, long timestamp, String group, String meta, String newFile) throws IOException {

    Put put = new Put(Bytes.toBytes(generateRowKey(node, group, timestamp)));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.META_COLUMN_QUALIFIER, Bytes.toBytes(meta));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.FILE_CONTENT_COLUMN_QUALIFIER, Bytes.toBytes(newFile));
    fileBufferedMutator.mutate(put);
  }

  public EventPojo getFileEvent(String node, long timestamp, String group) throws IOException {
    Table table = connection.getTable(HBaseConst.FILE_CHANGE_TABLE);

    Get get = new Get(Bytes.toBytes(generateRowKey(node, group, timestamp)));

    Result result = table.get(get);

    EventPojo event = new EventPojo(node, timestamp, group,
            Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.META_COLUMN_QUALIFIER)),
            Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.FILE_CONTENT_COLUMN_QUALIFIER)));

    table.close();
    return event;
  }

  public EventPojo getWebPrettyClosestFileEvent(String node, long endingTimestamp, String group) throws IOException {
    EventPojo pojo = getClosestFileEvent(node, endingTimestamp, group);
    pojo.setNewFile(pojo.getNewFile().replaceAll("\n", "</td></tr><tr><td>"));
    return pojo;
  }

  public EventPojo getClosestFileEvent(String node, long endingTimestamp, String group) throws IOException {
    Table table = connection.getTable(HBaseConst.FILE_CHANGE_TABLE);

    Scan scan = new Scan();
    scan.setStartRow(Bytes.toBytes(generateRowKey(node, group, endingTimestamp)));
    //TODO
    scan.setCaching(1);

    final ResultScanner scanner = table.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    EventPojo event = null;

    if (iterator.hasNext()) {
      final Result result = iterator.next();


      String rowKey = Bytes.toString(result.getRow());
      long timestamp = Long.MAX_VALUE - Long.parseLong(rowKey.substring(rowKey.lastIndexOf('|') + 1));


      event = new EventPojo(node, timestamp, group,
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.META_COLUMN_QUALIFIER)),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.FILE_CONTENT_COLUMN_QUALIFIER)));
    }


    table.close();

    return event;
  }

  public List<EventPojo> getFileEventList(String node, long endingTimestamp, String group, int maxVersions) throws IOException {
    List<EventPojo> eventList = new ArrayList<EventPojo>();

    Table table = connection.getTable(HBaseConst.FILE_CHANGE_TABLE);

    Scan scan = new Scan();
    scan.setStartRow(Bytes.toBytes(generateRowKey(node, group, endingTimestamp)));
    //TODO
    scan.setCaching(10);

    final ResultScanner scanner = table.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    int counter = 0;
    while (iterator.hasNext() && counter++ < maxVersions) {
      final Result result = iterator.next();

      String rowKey = Bytes.toString(result.getRow());
      long timestamp = Long.MAX_VALUE - Long.parseLong(rowKey.substring(rowKey.lastIndexOf('|') + 1));

      eventList.add(new EventPojo(node, timestamp, group,
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.META_COLUMN_QUALIFIER)),
              Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.FILE_CONTENT_COLUMN_QUALIFIER))));
    }

    table.close();
    return eventList;
  }

  Random r = new Random();
  static ExecutorService threadPool = Executors.newFixedThreadPool(20);
  public String generateNodeGraphJson(String node, long startingTime, long endingTime, String group) throws IOException {
    List<EventPojo> eventList = getNetFlowEventWindow(node, startingTime, endingTime, group);

    int nodeSeqId = 0;

    Set<String> distNodeSet = new HashSet<String>();
    HashMap<String, GraphNodeCounters> nodeNumberMap = new HashMap<String, GraphNodeCounters>();
    HashMap<String, Integer> edgeMap = new HashMap<String, Integer>();

    distNodeSet.add(node);
    GraphNodeCounters currentNodeCounter = new GraphNodeCounters(node, nodeSeqId++, 0, 1);
    nodeNumberMap.put(node, currentNodeCounter);

    for (EventPojo eventPojo: eventList) {
      for (NetFlowPojo netFlowPojo: eventPojo.getNetFlowPojoList()) {
        currentNodeCounter.bytesSent += netFlowPojo.getNumberOfBytes();

        if (distNodeSet.add(netFlowPojo.getDestinationAddress())) {
          nodeNumberMap.put(netFlowPojo.getDestinationAddress(), new GraphNodeCounters(netFlowPojo.getDestinationAddress(), nodeSeqId++, 0, 2));
          System.out.println("primary:" + netFlowPojo.getDestinationAddress() + ":" + (nodeSeqId-1));
        }
        edgeMap.put("0," + nodeNumberMap.get(netFlowPojo.getDestinationAddress()).id, 1);
      }
    }

    Set<String> loopSet = new HashSet<String>();
    loopSet.addAll(distNodeSet);

    for (String distNode: loopSet) {
      List<EventPojo> distEventList = getNetFlowEventWindow(distNode, startingTime, endingTime, group);
      HashSet<String> newNodeOnThisLoop = new HashSet<String>();
      for (EventPojo eventPojo: distEventList) {
        for (NetFlowPojo netFlowPojo: eventPojo.getNetFlowPojoList()) {
          if (newNodeOnThisLoop.add(netFlowPojo.getDestinationAddress())) {
            if (distNodeSet.add(netFlowPojo.getDestinationAddress())) {
              nodeNumberMap.put(netFlowPojo.getDestinationAddress() , new GraphNodeCounters(netFlowPojo.getDestinationAddress(), nodeSeqId++, 0, 3));
              //System.out.println("secondary:" + netFlowPojo.getDestinationAddress() + ":" + (nodeSeqId - 1));
            }

            int distNodeId = nodeNumberMap.get(distNode).id;
            int targetNodeId = nodeNumberMap.get(netFlowPojo.getDestinationAddress()).id;

            //System.out.println("Second Link:" + distNodeId + ":" + targetNodeId);

            if (distNodeId != targetNodeId) {
              int smallerNodeId = Math.min(distNodeId, targetNodeId);
              int maxNodeId = Math.max(distNodeId, targetNodeId);
              edgeMap.put(maxNodeId + "," + smallerNodeId, 1);
            }
          }
        }

      }
    }

    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("{\n" +"  \"nodes\":[\n");

    boolean isFirst = true;

    Map<Integer, GraphNodeCounters> orderedNodeTreeMap = new TreeMap<Integer, GraphNodeCounters>();

    for (Map.Entry<String, GraphNodeCounters> entry: nodeNumberMap.entrySet()) {
      orderedNodeTreeMap.put(entry.getValue().id, entry.getValue());
    }

    for (GraphNodeCounters nodePojo: orderedNodeTreeMap.values()) {
      if (isFirst) {
        isFirst = false;
      } else {
        strBuilder.append(",\n");
      }
      strBuilder.append("   {\"name\":\"" + nodePojo.node + "\",\"group\":" + (3-nodePojo.distFromAskedNode) + "}");
    }

    strBuilder.append("],\n" + "  \"links\":[\n");

    isFirst = true;
    for (Map.Entry<String, Integer> entry: edgeMap.entrySet()) {
//{"source":1,"target":0,"value":1},
      if (isFirst) {
        isFirst = false;
      } else {
        strBuilder.append(",\n");
      }
      int commaIndex = entry.getKey().indexOf(',');
      String source = entry.getKey().substring(0, commaIndex);
      String target = entry.getKey().substring(commaIndex + 1);
      strBuilder.append("   {\"source\":" + source + ",\"target\":" + target + ",\"value\":" + (source.equals("0")?5:1) + "}");
    }
    strBuilder.append("\n  ]\n" + "}");

    return strBuilder.toString();
  }

  // Increment increment = new Increment(Bytes.toBytes(generateRowKey(node, group, timestamp)));
  // increment.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes(netFlowPojo.generateColumn()), netFlowPojo.getNumberOfBytes());
  public List<EventPojo> getNetFlowEventWindow(String node, long startingTime, long endingTime, String group) throws IOException {
    List<EventPojo> results = new ArrayList<EventPojo>();

    Table detailedLineGraphTable = connection.getTable(HBaseConst.DETAILED_LINE_GRAPH_TABLE);

    Scan scan = new Scan();
    scan.setStartRow(Bytes.toBytes(generateRowKey(node, group, endingTime)));
    scan.setStopRow(Bytes.toBytes(generateRowKey(node, group, startingTime)));
    scan.setCaching(500);

    final ResultScanner scanner = detailedLineGraphTable.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    final List<NetFlowPojo> netFlowList = new ArrayList<NetFlowPojo>();

    int counter = 0;
    while (iterator.hasNext() && counter++ <= 500) {
      final Result result = iterator.next();

      String rowKey = Bytes.toString(result.getRow());
      int lastPipe = rowKey.lastIndexOf('|');
      long rowTimeStamp = Long.parseLong(rowKey.substring(lastPipe + 1));

      final NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(HBaseConst.BASE_COLUMN_FAMILY);

      for (Map.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
        netFlowList.add(new NetFlowPojo(node, entry.getKey(), entry.getValue()));
      }

      EventPojo eventPojo = new EventPojo(node, rowTimeStamp, netFlowList, group);

      results.add(eventPojo);
    }

    detailedLineGraphTable.close();

    return results;
  }

  public List<EventPojo> getDetailedGraphWindow(String node, long startingTime, long endingTime, String group) throws IOException {
    List<EventPojo> results = new ArrayList<EventPojo>();



    Table detailedLineGraphTable = connection.getTable(HBaseConst.DETAILED_LINE_GRAPH_TABLE);

    Scan scan = new Scan();
    scan.setStartRow(Bytes.toBytes(generateRowKey(node, group, endingTime)));
    scan.setStopRow(Bytes.toBytes(generateRowKey(node, group, startingTime)));
    scan.setCaching(100);


    final ResultScanner scanner = detailedLineGraphTable.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    int counter = 0;
    while (iterator.hasNext() && counter++ <= 500) {
      final Result result = iterator.next();

      List<SamplePojo> samples = SamplePojo.readListOfSamplePojos(Bytes.toString(result.getValue(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.BASE_COLUMN_QUALIFIER)));

      String rowKey = Bytes.toString(result.getRow());
      int lastPipe = rowKey.lastIndexOf('|');
      long rowTimeStamp = Long.parseLong(rowKey.substring(lastPipe + 1));

      EventPojo eventPojo = new EventPojo(node, rowTimeStamp, group, samples);

      results.add(eventPojo);
    }

    detailedLineGraphTable.close();

    return results;
  }


  public String getDetailedGraphWindowTSV(String node, long startingTime, long endingTime, String group, long timeInterval, boolean doCarryOverForMissing) throws IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");

    List<EventPojo> events = getDetailedGraphWindow(node, startingTime, endingTime, group);

    StringBuilder headerBuilder = new StringBuilder();
    StringBuilder dataBuilder = new StringBuilder();

    if (events.size() > 0) {
      long currentTimeStamp = events.get(0).getTimestamp();

      //get all columns
      HashSet<String> columns = new HashSet<String>();
      for (EventPojo event : events) {
        for (SamplePojo sample : event.getSamples()) {
          columns.add(sample.getKey());
          if (sample.getMax() != null) {
            columns.add(sample.getKey() + ".Max");
          }
        }
      }

      //currentValues
      TreeMap<String, NumberHolder> columnValueMap = new TreeMap<String, NumberHolder>();
      boolean isFirst = true;

      headerBuilder.append("date");
      for (String column : columns) {
        columnValueMap.put(column, new NumberHolder());
        headerBuilder.append("\t" + column);
      }

      for (EventPojo event : events) {
        if (event.getTimestamp() >= currentTimeStamp &&
                event.getTimestamp() < currentTimeStamp + timeInterval) {
          for (SamplePojo sample : event.getSamples()) {
            columnValueMap.get(sample.getKey()).addValue(Long.parseLong(sample.getValue()));
            if (sample.getMax() != null) {
              columnValueMap.get(sample.getKey() + ".Max").addValue(Long.parseLong(sample.getMax()));
            }
          }
        } else {

          //printvalues
          dataBuilder.append(dateFormat.format(new Date(currentTimeStamp)));
          for (Map.Entry<String, NumberHolder> entry : columnValueMap.entrySet()) {
            dataBuilder.append("\t" + entry.getValue().getValue());
            if (!doCarryOverForMissing) {
              entry.getValue().resetValue();
            }
          }
          dataBuilder.append(System.lineSeparator());
          //finished print

          //increment time
          currentTimeStamp += timeInterval;
          while (event.getTimestamp() >= currentTimeStamp + timeInterval) {
            //print gap record
            dataBuilder.append(dateFormat.format(new Date(currentTimeStamp)));
            for (Map.Entry<String, NumberHolder> entry : columnValueMap.entrySet()) {
              dataBuilder.append("\t" + entry.getValue().getValue());
            }
            dataBuilder.append("\t GAP" + event.getTimestamp() + " " + currentTimeStamp + System.lineSeparator());
            //finished gap print

            //increment time
            currentTimeStamp += timeInterval;
          }

          if (doCarryOverForMissing) {
            for (Map.Entry<String, NumberHolder> entry : columnValueMap.entrySet()) {
              entry.getValue().resetValue();
            }
          }

          if (event.getTimestamp() >= currentTimeStamp &&
                  event.getTimestamp() < currentTimeStamp + timeInterval) {
            for (SamplePojo sample : event.getSamples()) {
              columnValueMap.get(sample.getKey()).addValue(Long.parseLong(sample.getValue()));
              if (sample.getMax() != null) {
                columnValueMap.get(sample.getKey() + ".Max").addValue(Long.parseLong(sample.getMax()));
              }
            }
          }
        }
      }
      //printing the end
      dataBuilder.append(dateFormat.format(new Date(currentTimeStamp)));
      for (Map.Entry<String, NumberHolder> entry : columnValueMap.entrySet()) {
        dataBuilder.append("\t" + entry.getValue().getValue());
      }

      return headerBuilder.toString() + System.lineSeparator() + dataBuilder.toString();
    } else {
      return "- - -";
    }
  }

  private String generateRowKey(String node, String group, long timestamp) {
    return node + "|" + group + "|" + (Long.MAX_VALUE - timestamp);
  }

  private class NumberHolder {
    long value = 0;
    int count = 0;

    public void addValue(long newValue) {
      value += newValue;
      count++;
    }

    public void resetValue() {
      count = 0;
      value = 0;
    }

    public long getValue() {
      return value/count;
    }
  }

  public void putNotifications(List<NotificationPojo> notificationPojos) throws IOException {
    if (notificationPojos.size() > 0) {
      Table table = connection.getTable(HBaseConst.NOTIFICATION_TABLE);

      List<Put> putList = new ArrayList<Put>();

      for (NotificationPojo pojo : notificationPojos) {
        Put put = new Put(Bytes.toBytes(Long.MAX_VALUE - pojo.getTimeOfNotification()));
        put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, Bytes.toBytes(pojo.toString()), HBaseConst.BASE_COLUMN_QUALIFIER);
        putList.add(put);
      }

      table.put(putList);

      table.close();
    }
  }

  public List<NotificationPojo> getLastNNotifications(int lastN) throws IOException {
    List<NotificationPojo> notificationPojos = new ArrayList<NotificationPojo>();

    Table table = connection.getTable(HBaseConst.NOTIFICATION_TABLE);

    Scan scan = new Scan();
    scan.setCaching(lastN);
    scan.setCaching(lastN);

    ResultScanner scanner = table.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    while (iterator.hasNext()) {
      final Result result = iterator.next();

      final NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(HBaseConst.BASE_COLUMN_FAMILY);
      for (byte[] key: familyMap.keySet()) {
        notificationPojos.add(new NotificationPojo(Bytes.toString(key)));
        if (notificationPojos.size() >= lastN) { break; }
      }
      if (notificationPojos.size() >= lastN) { break; }
    }

    table.close();
    return notificationPojos;
  }

  public void putRule(RulePojo rule) throws IOException {

    System.out.println("PutRule:" + rule.toPrettyString());

    Table table = connection.getTable(HBaseConst.RULES_TABLE);

    Put put = new Put(Bytes.toBytes(rule.getRuleId()));
    put.addColumn(HBaseConst.BASE_COLUMN_FAMILY, HBaseConst.BASE_COLUMN_QUALIFIER, Bytes.toBytes(rule.toString()));

    table.put(put);
    table.close();
  }

  public List<RulePojo> getAllRules() throws IOException {
    List<RulePojo> rulePojoList = new ArrayList<RulePojo>();

    Table table = connection.getTable(HBaseConst.RULES_TABLE);

    Scan scan = new Scan();
    scan.setStartRow(Bytes.toBytes(""));

    ResultScanner scanner = table.getScanner(scan);

    final Iterator<Result> iterator = scanner.iterator();

    System.out.println("getEventList");

    while (iterator.hasNext()) {
      final Result result = iterator.next();

      final NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(HBaseConst.BASE_COLUMN_FAMILY);
      for (byte[] value: familyMap.values()) {
        RulePojo rulePojo = new RulePojo(Bytes.toString(result.getRow()), Bytes.toString(value));
        //System.out.println(rulePojo.toPrettyString());
        rulePojoList.add(rulePojo);
      }
    }

    table.close();
    return rulePojoList;
  }

  public void generateTables() throws IOException {
    Admin admin = connection.getAdmin();

    HTableDescriptor graphTable = new HTableDescriptor(HBaseConst.DETAILED_LINE_GRAPH_TABLE);
    HTableDescriptor nodeTable = new HTableDescriptor(HBaseConst.NODE_LIST_TABLE);
    HTableDescriptor fileTable = new HTableDescriptor(HBaseConst.FILE_CHANGE_TABLE);
    HTableDescriptor notificationsTable = new HTableDescriptor(HBaseConst.NOTIFICATION_TABLE);
    HTableDescriptor rulesTable = new HTableDescriptor(HBaseConst.RULES_TABLE);

    HColumnDescriptor columnFamily = new HColumnDescriptor(HBaseConst.BASE_COLUMN_FAMILY);
    //columnFamily.setCompressTags(true);
    //columnFamily.setCompressionType(Compression.Algorithm.SNAPPY);

    graphTable.addFamily(columnFamily);
    nodeTable.addFamily(columnFamily);
    fileTable.addFamily(columnFamily);
    notificationsTable.addFamily(columnFamily);
    rulesTable.addFamily(columnFamily);

    admin.createTable(graphTable);
    admin.createTable(nodeTable);
    admin.createTable(fileTable);
    admin.createTable(notificationsTable);
    admin.createTable(rulesTable);
  }

  public void dropTables() throws IOException {
    Admin admin = connection.getAdmin();

    admin.disableTable(HBaseConst.DETAILED_LINE_GRAPH_TABLE);
    admin.disableTable(HBaseConst.NODE_LIST_TABLE);
    admin.disableTable(HBaseConst.FILE_CHANGE_TABLE);
    admin.disableTable(HBaseConst.NOTIFICATION_TABLE);
    admin.disableTable(HBaseConst.RULES_TABLE);

    admin.deleteTable(HBaseConst.DETAILED_LINE_GRAPH_TABLE);
    admin.deleteTable(HBaseConst.NODE_LIST_TABLE);
    admin.deleteTable(HBaseConst.FILE_CHANGE_TABLE);
    admin.deleteTable(HBaseConst.NOTIFICATION_TABLE);
    admin.deleteTable(HBaseConst.RULES_TABLE);

  }
}
