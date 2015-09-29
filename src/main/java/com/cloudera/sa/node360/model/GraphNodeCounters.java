package com.cloudera.sa.node360.model;

/**
 * Created by ted.malaska on 6/8/15.
 */
public class GraphNodeCounters  implements java.io.Serializable{
  public String node;
  public int id;
  public long bytesSent;
  public int distFromAskedNode;

  public GraphNodeCounters(String node, int id, long bytesSent, int distFromAskedNode) {
    this.node = node;
    this.id = id;
    this.bytesSent = bytesSent;
    this.distFromAskedNode = distFromAskedNode;

  }
}
