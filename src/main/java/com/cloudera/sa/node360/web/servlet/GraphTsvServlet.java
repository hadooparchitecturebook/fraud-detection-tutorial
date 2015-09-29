package com.cloudera.sa.node360.web.servlet;

import com.cloudera.sa.node360.service.HBaseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by ted.malaska on 6/5/15.
 */
public class GraphTsvServlet extends HttpServlet
{
  static HBaseService service;

  public static void setHBaseService(HBaseService s) {
    service = s;
  }
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    System.out.println("TSV");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy.HH.mm.ss");
    response.setContentType("text/tab-separated-values");
    response.setStatus(HttpServletResponse.SC_OK);

    String node = request.getParameter("node");
    String group = request.getParameter("grp");
    try {
      long sTime = dateFormat.parse(request.getParameter("sTime")).getTime();
      long eTime = dateFormat.parse(request.getParameter("eTime")).getTime();

      System.out.println(node + "," + group + "," + sTime + "," + eTime);

      response.getWriter().println(service.getDetailedGraphWindowTSV(node, sTime, eTime, group, 5000, true));
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }
}

