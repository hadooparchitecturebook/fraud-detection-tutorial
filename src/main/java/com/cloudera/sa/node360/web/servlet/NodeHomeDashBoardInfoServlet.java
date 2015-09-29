package com.cloudera.sa.node360.web.servlet;

import com.cloudera.sa.node360.model.NodeStatusPojo;
import com.cloudera.sa.node360.model.SamplePojo;
import com.cloudera.sa.node360.service.HBaseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by ted.malaska on 6/11/15.
 */
public class NodeHomeDashBoardInfoServlet  extends HttpServlet
{
  static HBaseService service;

  public static void setHBaseService(HBaseService s) {
    service = s;
  }
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy.HH.mm.ss");
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);

    String node = request.getParameter("node");

    try {
      final NodeStatusPojo nodeStatus = service.getNodeStatus(node);

      StringBuilder strBuilder = new StringBuilder();

      boolean isFirst = true;

      for (Map.Entry<String, Map<String, SamplePojo>> stringMapEntry : nodeStatus.getGroupSampleMap().entrySet()) {
        String group = stringMapEntry.getKey();
        for (Map.Entry<String, SamplePojo> stringSamplePojoEntry : stringMapEntry.getValue().entrySet()) {

          String key = stringSamplePojoEntry.getKey();
          String value = stringSamplePojoEntry.getValue().getValue();
          String max = stringSamplePojoEntry.getValue().getMax();

          if (key.equals("group")) {max = "100";}

          if (isFirst) {
            isFirst = false;
          } else {
            strBuilder.append("\n");
          }
          strBuilder.append(group + "," + key + "," + value + "," + max);
        }
      }
      response.getWriter().println(strBuilder);

    } catch (Exception e) {
      e.printStackTrace();
    }



  }
}
