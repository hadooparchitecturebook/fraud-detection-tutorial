package com.cloudera.sa.node360.service.utils;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by ted.malaska on 6/7/15.
 */
public class HBaseScanThreadUtil {
  static ExecutorService pool = Executors.newFixedThreadPool(20);

  static public List<Result> getNextResult(List<byte[]> keys, Connection connection, TableName tableName) throws Exception {

    List<GetNextResult> callables = new ArrayList<GetNextResult>();
    List<Result> results = new ArrayList<Result>();
    for (byte[] rowKey: keys) {
      callables.add(new GetNextResult(rowKey, connection, tableName));
    }

    final List<Future<Result>> futures = pool.invokeAll(callables);

    for (Future<Result> future: futures) {
      results.add(future.get());
    }
    return results;
  }

  static public class GetNextResult implements Callable<Result> {

    byte[] rowKey;
    Connection connection;
    TableName tableName;

    public GetNextResult(byte[] rowKey, Connection connection, TableName tableName){
      this.rowKey = rowKey;
      this.connection = connection;
      this.tableName = tableName;
    }

    @Override
    public Result call() throws Exception {
      Table table = connection.getTable(tableName);

      Scan scan = new Scan();
      scan.setStartRow(rowKey);
      scan.setCaching(1);

      final ResultScanner scanner = table.getScanner(scan);

      return scanner.next();
    }
  }

}
