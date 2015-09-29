package com.cloudera.sa.node360.playarea;

import com.cloudera.sa.node360.localservers.LocalHBaseCluster;
import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.NetFlowPojo;
import com.cloudera.sa.node360.model.NodePojo;
import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.cloudera.sa.node360.nodepuller.listener.InternalQueueListener;
import com.cloudera.sa.node360.nodepuller.listener.SystemOutListener;
import com.cloudera.sa.node360.service.HBaseService;
import com.cloudera.sa.node360.service.deamon.QueueToHBaseProcess;
import com.cloudera.sa.node360.symulator.NodeDataSymulator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.net.StaticMapping;

import java.io.IOException;
import java.util.List;

/**
 * Created by ted.malaska on 6/5/15.
 */
public class BackEndTests {

  public static long startTime = System.currentTimeMillis();

  public static void main(String args[]) throws Exception {

    LocalHBaseCluster hbaseCluster = new LocalHBaseCluster();
    Configuration config = hbaseCluster.getConfiguration();
    QueueToHBaseProcess queueHBaseProcess = null;
    try {
      System.out.println("Step 1: Config");

      HBaseService hbaseService = new HBaseService(config);

      System.out.println("Step 2: Service");

      hbaseService.generateTables();

      System.out.println("Step 2.5: Tables");

      populateInitialNodes(hbaseService);

      System.out.println("Step 3: Nodes Populated");

      EventListener listener = new InternalQueueListener();

      System.out.println("Step 4: Listener");

      NodeDataSymulator sym = new NodeDataSymulator(listener, hbaseService,
              20, 20, 100, 7);

      queueHBaseProcess = new QueueToHBaseProcess(hbaseService);

      queueHBaseProcess.start();

      sym.run(20);

      Thread.sleep(1000);

      System.out.println("Step 5: Sym");

      runFetchesOnTheSystem(hbaseService);



      System.out.println("Step 6: API Test");
    } finally {
      queueHBaseProcess.stop();
      hbaseCluster.shutDown();

      System.out.println("Step 7: Shut Down");
    }
  }

  public static void populateInitialNodes(HBaseService service) throws IOException {
    service.addNode(new NodePojo("127.1.1.1", "foo1", "20878", "A"));
    service.addNode(new NodePojo("127.1.1.2", "foo2", "20878", "A"));
    service.addNode(new NodePojo("127.1.1.3", "foo3", "20870", "B"));
    service.addNode(new NodePojo("127.1.1.4", "foo4", "20870", "B"));
    service.addNode(new NodePojo("127.1.2.5", "foo5", "20855", "C"));
    service.addNode(new NodePojo("127.1.2.6", "foo6", "20870", "C"));
    service.addNode(new NodePojo("127.1.2.7", "foo7", "20870", "C"));
    service.addNode(new NodePojo("127.1.2.8", "foo8", "20870", "C"));
  }

  public static void runFetchesOnTheSystem(HBaseService service) throws IOException {
    StaticMapping g;
    System.out.println("FullNodeList");
    List<NodePojo> fullNodeList = service.getFullNodeList();
    for (NodePojo node: fullNodeList) {
      System.out.println(" - " + node);
    }
    System.out.println();
    System.out.println("ListFileEvents");
    final List<EventPojo> fileEventList = service.getFileEventList("127.1.1.2", System.currentTimeMillis() - 100000, "/etc/hosts", 10);
    for (EventPojo event: fileEventList) {
      System.out.println(" - " + event.getMeta() + " : " + event.getNewFile());
    }
    System.out.println();
    System.out.println("AutoCompleteList");
    List<NodePojo> autoCompleteNodeList = service.getAutoCompleteNodeList("127.1.1", 10);
    for (NodePojo node: autoCompleteNodeList) {
      System.out.println(" - " + node);
    }
    System.out.println();
    System.out.println("TSV CPU Graph");
    System.out.println(service.getDetailedGraphWindowTSV("127.1.1.3", startTime, startTime + 5000 * 80, "cpu", 5000, true));
    System.out.println();
    System.out.println("TSV drives Graph");
    System.out.println(service.getDetailedGraphWindowTSV("127.1.1.3", startTime, startTime + 5000 * 80, "drives", 5000, true));
    System.out.println();
    System.out.println("TSV NetFlow");

    List<EventPojo> netflowEventList = service.getNetFlowEventWindow("127.1.1.3", startTime, startTime + 5000 * 80, "netflow");
    for (EventPojo pojo: netflowEventList) {
      for (NetFlowPojo netFlowPojo : pojo.getNetFlowPojoList()) {
        System.out.println(" -- :" + netFlowPojo);
      }
    }

    System.out.println(service.generateNodeGraphJson("127.1.1.3", startTime, startTime + 5000 * 80, "netflow"));
  }
}
