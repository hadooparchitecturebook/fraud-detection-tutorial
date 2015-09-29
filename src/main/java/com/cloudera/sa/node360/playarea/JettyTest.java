package com.cloudera.sa.node360.playarea;


import com.cloudera.sa.node360.localservers.LocalHBaseCluster;
import com.cloudera.sa.node360.model.NodePojo;
import com.cloudera.sa.node360.model.RulePojo;
import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.cloudera.sa.node360.nodepuller.listener.InternalQueueListener;
import com.cloudera.sa.node360.service.HBaseService;
import com.cloudera.sa.node360.service.deamon.QueueToHBaseProcess;
import com.cloudera.sa.node360.symulator.NodeDataSymulator;
import com.cloudera.sa.node360.web.servlet.*;
import org.apache.hadoop.conf.Configuration;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.IOException;

/**
 * Created by ted.malaska on 6/5/15.
 */
public class JettyTest {
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

      System.out.println("Step 2.75: Rules");

      //populateRules(hbaseService);

      System.out.println("Step 3: Nodes Populated");

      EventListener listener = new InternalQueueListener();

      System.out.println("Step 4: Listener");

      long sleepBetweenIterations = 5000;
      long maxIterations = 200;
      int updateNodeListEveryNIterations = 100;
      int updateEtcFileEveryNRequest = 7;

      NodeDataSymulator sym = new NodeDataSymulator(listener, hbaseService,
              sleepBetweenIterations, maxIterations,
              updateNodeListEveryNIterations, updateEtcFileEveryNRequest);

      queueHBaseProcess = new QueueToHBaseProcess(hbaseService);

      queueHBaseProcess.start();

      sym.run(10);

      System.out.println("Step 5: Sym");


      System.out.println("Step 6: API Test");

      Server server = new Server(8082);

      WebAppContext webapp = new WebAppContext();
      webapp.setContextPath("/fe");
      webapp.setResourceBase("./webapp");
      webapp.setWelcomeFiles(new String[]{"index.html"});


      Context servletContext = new Context();
      servletContext.setContextPath("/be");
      servletContext.addServlet(GraphTsvServlet.class, "/graph.tsv");
      servletContext.addServlet(FileGetterServlet.class, "/file/*");
      servletContext.addServlet(NodeAutoCompleteServlet.class, "/auto/*");
      servletContext.addServlet(GraphNodeJsonServlet.class, "/nodeGraph/*");
      servletContext.addServlet(NodeHomeDashBoardInfoServlet.class, "/nodeDash/*");

      server.setHandlers(new Handler[]{webapp, servletContext});

      System.out.println("-");

      GraphTsvServlet.setHBaseService(hbaseService);
      FileGetterServlet.setHBaseService(hbaseService);
      NodeAutoCompleteServlet.setHBaseService(hbaseService);
      GraphNodeJsonServlet.setHBaseService(hbaseService);
      NodeHomeDashBoardInfoServlet.setHBaseService(hbaseService);

      server.start();
      server.join();

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

  public static void populateRules(HBaseService service) throws IOException {

    String ruleId = "42";
    String sourcePort = null;
    String destinationPort = null;
    String destinationIp = "127.1.2.8";

    RulePojo rule = new RulePojo(ruleId, sourcePort, destinationPort, destinationIp);

    service.putRule(rule);
  }


}
