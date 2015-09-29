package com.cloudera.sa.node360.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ted.malaska on 6/7/15.
 */
public class NodeStatusPojo  implements java.io.Serializable{
  NodePojo node;
  Map<String, Map<String, SamplePojo>> groupSampleMap;

  public NodeStatusPojo(NodePojo node, Map<String, Map<String, SamplePojo>> groupSampleMap) {
    this.node = node;
    this.groupSampleMap = groupSampleMap;
  }

  public NodePojo getNode() {
    return node;
  }

  public void setNode(NodePojo node) {
    this.node = node;
  }

  public Map<String, Map<String, SamplePojo>> getGroupSampleMap() {
    return groupSampleMap;
  }

  public void setGroupSampleMap(Map<String, Map<String, SamplePojo>> groupSampleMap) {
    this.groupSampleMap = groupSampleMap;
  }
}
