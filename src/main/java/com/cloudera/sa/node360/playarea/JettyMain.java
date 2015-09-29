package com.cloudera.sa.node360.playarea;

import com.cloudera.sa.node360.service.HBaseService;
import com.cloudera.sa.node360.web.servlet.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.IOException;

/**
 * Created by ted.malaska on 6/12/15.
 */
public class JettyMain {
  public static void main(String args[]) throws Exception {

    if (args.length == 0) {
      System.out.println("JettyMain {port} {zookeeperQuorum}");
    }
    try {
      Configuration config = HBaseConfiguration.create();
      config.addResource("/etc/hbase/conf/hbase-site.xml");
      config.set(HConstants.ZOOKEEPER_QUORUM, args[1]);
      //config.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");

      HBaseService hbaseService = new HBaseService(config);

      Server server = new Server(Integer.parseInt(args[0]));

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
      System.out.println("Step 7: Shut Down");
    }
  }
}
