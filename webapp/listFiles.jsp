<%@ page import="com.cloudera.sa.node360.service.HBaseService" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudera.sa.node360.model.*" %>
<HTML>
<HEAD>
  <TITLE>List Files</TITLE>
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

List<EventPojo> eventList = service.getFileEventList(node, sTime, group, 10);
%>

<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a> > <a href="/fe/nodeHome.jsp?node=<%= node%>"><%= node%></a> > <%= group%>
</td></tr></table>

<H3>Recent Versions of <%= group%> on <%= node%></H3>

<TABLE WIDTH="100%" id="t01"><TR>
  <th>Node</th>
  <th>TimeStamp</th>
  <th>Group</th>
  <th>Meta</th>
  <th>Actions</th>
</tr>

<%


for (int i = 0; i < eventList.size(); i++) {
  EventPojo pojo = eventList.get(i);
  EventPojo nextPojo = null;
  if (i < eventList.size() -1) {
    nextPojo = eventList.get(i+1);
  }
%>

<tr>
  <td><%= pojo.getNode()%></td>
  <td><%= prettyDateFormat.format(new Date(pojo.getTimestamp()))%></td>
  <td><%= pojo.getGroup()%></td>
  <td><%= pojo.getMeta()%></td>
  <td>
   <a href="/fe/showFile.jsp?node=<%= pojo.getNode()%>&grp=<%= pojo.getGroup()%>&sTime=<%= dateFormat.format(new Date(pojo.getTimestamp()))%>">View</a>
   <a href="/be/file/<%= pojo.getMeta().substring(pojo.getMeta().lastIndexOf(' ') + 1)%>?node=<%= pojo.getNode()%>&grp=<%= pojo.getGroup()%>&sTime=<%= dateFormat.format(new Date(pojo.getTimestamp()))%>">Download</a>
   <% if (i < eventList.size() -1) { %>
   <a href="/fe/diffFiles.jsp?node1=<%= pojo.getNode()%>&grp1=<%= pojo.getGroup()%>&sTime1=<%= dateFormat.format(new Date(pojo.getTimestamp()))%>&node2=<%= nextPojo.getNode()%>&grp2=<%= nextPojo.getGroup()%>&sTime2=<%= dateFormat.format(new Date(nextPojo.getTimestamp()))%>">Compare</a>
   <% } %>
  </td>
</tr>
<%
}
%>
</table>
</BODY>


