package com.cloudera.sa.node360.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class EventPojo implements java.io.Serializable{

  public static String LINE_TYPE = "L";
  public static String FILE_TYPE = "F";
  public static String NETFLOW_TYPE = "N";

  String type;
  String node;
  long timestamp;
  String group;
  List<SamplePojo> samples;
  String meta;
  String newFile;
  List<NetFlowPojo> netFlowPojoList;

  public EventPojo(String node, long timestamp, String group, List<SamplePojo> samples) {
    type = LINE_TYPE;
    this.node = node;
    this.timestamp = timestamp;
    this.group = group;
    this.samples = samples;
  }

  public EventPojo(String node, long timestamp, String group, String meta, String newFile) {
    type = FILE_TYPE;
    this.node = node;
    this.timestamp = timestamp;
    this.group = group;
    this.meta = meta;
    this.newFile = newFile;
  }

  public EventPojo(String node, long timestamp, String group, NetFlowPojo netFlowPojo) {
    type = NETFLOW_TYPE;
    this.node = node;
    this.timestamp = timestamp;
    this.group = group;
    this.netFlowPojoList = new ArrayList<NetFlowPojo>();
    netFlowPojoList.add(netFlowPojo);
  }

  public EventPojo(String node, long timestamp, List<NetFlowPojo> netFlowPojoList, String group) {
    type = NETFLOW_TYPE;
    this.node = node;
    this.timestamp = timestamp;
    this.group = group;
    this.netFlowPojoList = netFlowPojoList;
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public List<SamplePojo> getSamples() {
    return samples;
  }

  public void setSamples(List<SamplePojo> samples) {
    this.samples = samples;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta(String meta) {
    this.meta = meta;
  }

  public String getNewFile() {
    return newFile;
  }

  public void setNewFile(String newFile) {
    this.newFile = newFile;
  }

  public String getType() {
    return type;
  }

  public List<NetFlowPojo> getNetFlowPojoList() {
    return netFlowPojoList;
  }

  public void setNetFlowPojoList(List<NetFlowPojo> netFlowPojoList) {
    this.netFlowPojoList = netFlowPojoList;
  }
}
