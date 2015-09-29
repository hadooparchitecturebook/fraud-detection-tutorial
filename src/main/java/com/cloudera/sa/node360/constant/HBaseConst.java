package com.cloudera.sa.node360.constant;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class HBaseConst {
  static final public TableName NODE_LIST_TABLE = TableName.valueOf("NodeList");
  static final public TableName DETAILED_LINE_GRAPH_TABLE = TableName.valueOf("LineGraph");
  static final public TableName FILE_CHANGE_TABLE = TableName.valueOf("FileChange");
  static final public TableName NOTIFICATION_TABLE = TableName.valueOf("Notifications");
  static final public TableName RULES_TABLE = TableName.valueOf("Rules");

  static final public byte[] BASE_COLUMN_FAMILY = Bytes.toBytes("C");
  static final public byte[] BASE_COLUMN_QUALIFIER = Bytes.toBytes("c");

  static final public byte[] META_COLUMN_QUALIFIER = Bytes.toBytes("m");
  static final public byte[] FILE_CONTENT_COLUMN_QUALIFIER = Bytes.toBytes("C");
}
