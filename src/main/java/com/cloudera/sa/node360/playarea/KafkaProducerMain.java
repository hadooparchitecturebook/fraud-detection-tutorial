package com.cloudera.sa.node360.playarea;

import com.cloudera.sa.node360.nodepuller.listener.KafkaListener;
import com.cloudera.sa.node360.service.HBaseService;
import com.cloudera.sa.node360.symulator.NodeDataSymulator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


/**
 * Created by ted.malaska on 6/12/15.
 */
public class KafkaProducerMain {
  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {



    if (args.length == 0) {
      System.out.println("netFlowTopic, nodeStatusTopic, brokerList, sleepBetweenIterations, maxIterations, updateNodeListEveryNIterations, updateEtcFileEveryNRequest zookkeeperQuorum");
      return;
    }

    String netFlowTopic = args[0];
    String nodeStatusTopic = args[1];
    String brokerList = args[2];
    long sleepBetweenIterations = Long.parseLong(args[3]);//5000;
    long maxIterations = Long.parseLong(args[4]); //200;
    int updateNodeListEveryNIterations = Integer.parseInt(args[5]); //100;
    int updateEtcFileEveryNRequest = Integer.parseInt(args[6]); //7


    KafkaListener kafkaListener = new KafkaListener(netFlowTopic, nodeStatusTopic, brokerList);

    Configuration config = HBaseConfiguration.create();
    config.addResource("/etc/hbase/conf/hbase-site.xml");
    config.set(HConstants.ZOOKEEPER_QUORUM, args[7]);
    //config.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");

    HBaseService hbaseService = new HBaseService(config);

    NodeDataSymulator sym = new NodeDataSymulator(kafkaListener, hbaseService,
            sleepBetweenIterations, maxIterations,
            updateNodeListEveryNIterations, updateEtcFileEveryNRequest);

    sym.run(10);

    while (sym.isRunning()) {
      Thread.sleep(1000);
    }
  }
}
