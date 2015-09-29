package com.cloudera.sa.node360.playarea;

import com.cloudera.sa.node360.model.NodePojo;
import com.cloudera.sa.node360.service.HBaseService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ted.malaska on 6/12/15.
 */
public class HBaseAdminMain {
  public static void main(String[] args) throws IOException {

    if (args.length == 0) {
      System.out.println("HBaseAdminMain {action} {numberOfNodes} {zookeeperQuorum}");
      System.out.println("action can be \"create\" or \"drop\"");
      return;
    }

    Configuration config = HBaseConfiguration.create();
    config.addResource("/etc/hbase/conf/hbase-site.xml");
    config.set(HConstants.ZOOKEEPER_QUORUM, args[2]);
    config.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");

    HBaseService service = new HBaseService(config);

    if (args[0].equals("create")) {
      service.generateTables();

      int maxNumberOfNodes = Integer.parseInt(args[1]);
      ArrayList<NodePojo> nodeList = new ArrayList<NodePojo>();
      for (int i = 0; i < maxNumberOfNodes; i++) {
        int frontNumber = Math.max(1, Math.min((127 + i) / 127, 127));
        int backNumber = Math.abs(i % 127);

        nodeList.add(new NodePojo(frontNumber + ".1.1." + backNumber, "foo1", "20878", "A"));
        System.out.println(i + " : Created node: " + frontNumber + ".1.1." + backNumber);

      }
      service.addNodes(nodeList);
    } else {
      service.dropTables();
    }
    System.out.println("Finished");
  }
}
