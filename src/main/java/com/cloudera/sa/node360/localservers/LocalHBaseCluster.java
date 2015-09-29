package com.cloudera.sa.node360.localservers;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;

/**
 * Created by ted.malaska on 6/5/15.
 */
public class LocalHBaseCluster {
  HBaseTestingUtility htu1;

  public LocalHBaseCluster() throws Exception {
    htu1 = new HBaseTestingUtility();
    htu1.getConfiguration().set(HConstants.ZOOKEEPER_ZNODE_PARENT, "/x1");
    htu1.getConfiguration().set(HConstants.ZOOKEEPER_CLIENT_PORT,
            "64410");
    htu1.getConfiguration().set(HConstants.MASTER_INFO_PORT, "64310");

    htu1.startMiniCluster();
  }

  public Configuration getConfiguration() {
    return htu1.getConfiguration();
  }

  public void shutDown() throws Exception {
    htu1.shutdownMiniCluster();
  }
}
