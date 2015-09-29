<%@ page import="com.cloudera.sa.node360.service.HBaseService" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudera.sa.node360.model.*" %>

<HTML>
<HEAD>
  <TITLE>List Files</TITLE>
    <link rel="stylesheet" href="./js/jquery/jquery-ui.css">
    <link rel="stylesheet" href="./js/jquery/basicStyle.css">
    <script src="./js/jquery/jquery-1.11.3.js"></script>
    <script src="./js/jquery/jquery-ui.js"></script>
</HEAD>
<BODY>


<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a> > Rules List
</td></tr></table>

<%
HBaseService service = new HBaseService();
List<RulePojo> eventList = service.getAllRules();
%>

<a href="createRulePage.jsp">Create New Rule</a>

<H3>Rule List</H3>

<TABLE WIDTH="80%" id="t01">
  <TR>
    <th>Count</th>
    <th>Rule Id</th>
    <th>sourcePort</th>
    <th>destinationPort</th>
    <th>destinationIp</th>
  </tr>

  <%
    int counter = 1;
    for (RulePojo rulePojo: eventList) {
  %>
  <TR>
    <td><%= counter++%></td>
    <td><%= rulePojo.getRuleId()%></td>
    <td><%= rulePojo.getSourcePort()%></td>
    <td><%= rulePojo.getDestinationPort()%></td>
    <td><%= rulePojo.getDestinationIp()%></td>
  </tr>
  <%
    }
  %>

</table>


</body>
</html>