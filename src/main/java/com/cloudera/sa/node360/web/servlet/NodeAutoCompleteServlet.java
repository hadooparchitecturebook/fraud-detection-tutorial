package com.cloudera.sa.node360.web.servlet;

import com.cloudera.sa.node360.model.NodePojo;
import com.cloudera.sa.node360.service.HBaseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by ted.malaska on 6/7/15.
 */
public class NodeAutoCompleteServlet extends HttpServlet {
  static HBaseService service;

  public static void setHBaseService(HBaseService s) {
    service = s;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("AutoComplete");
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_OK);

    String node = request.getParameter("term");

    StringBuilder strBuilder = new StringBuilder("[");
    boolean isFirst = true;

    List<NodePojo> nodes = service.getAutoCompleteNodeList(node, 10);

    for (NodePojo nodePojo : nodes) {
      if (isFirst) {
        isFirst = false;
      } else {
        strBuilder.append(",");
      }
      strBuilder.append("{\"id\":\"n" + nodePojo.getIpAddress() + "\",\"label\":\"" + nodePojo.getIpAddress() + "\",\"value\":\"" + nodePojo.getIpAddress() + "\"}");
    }
    strBuilder.append("]");

    response.getWriter().println(strBuilder.toString());

  }
}