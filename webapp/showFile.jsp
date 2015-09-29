<%@ page import="com.cloudera.sa.node360.service.HBaseService" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudera.sa.node360.model.*" %>

<HTML>
<HEAD>
  <TITLE>Show File</TITLE>
    <link rel="stylesheet" href="./js/jquery/jquery-ui.css">
    <link rel="stylesheet" href="./js/jquery/basicStyle.css">
</HEAD>
<BODY>

<%
HBaseService service = new HBaseService();
SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy.HH.mm.ss");
SimpleDateFormat prettyDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

String node = request.getParameter("node");
String group = request.getParameter("grp");
long sTime = dateFormat.parse(request.getParameter("sTime")).getTime();

EventPojo event = service.getWebPrettyClosestFileEvent(node, sTime, group);

String file = event.getNewFile();

%>

<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a> > <a href="/fe/nodeHome.jsp?node=<%= node%>"><%= node%></a> > <a href="/fe/listFiles.jsp?node=<%= node%>&grp=<%= group%>&sTime=<%= request.getParameter("sTime")%>"><%= group%></a> > Show File
</td></tr></table>

<H3>File Details</H3>

<TABLE WIDTH="100%" id="t01">
  <TR>
    <TH>Name</TH>
    <TH>Value</TH>
  <TR>
    <TD>Node</TD>
    <TD><%= node%></td>
  </TR>
  <TR>
    <TD>Group</TD>
    <TD><%= group%></td>
  </TR>
  <TR>
    <TD>TimeStamp</TD>
    <TD><%= prettyDateFormat.format(new Date(event.getTimestamp()))%></td>
  </TR>
  <TR>
    <TD>Meta</TD>
    <TD><%= event.getMeta()%></div>
  </TR>
  <TR>
    <TD>Content</TD>
    <TD>
      <table><tr><td>
        <%= file%>
      </tr></td></table>
    </td>
  </TR>
</table>