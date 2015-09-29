<%@ page import="com.cloudera.sa.node360.service.HBaseService" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudera.sa.node360.model.*" %>
<!DOCTYPE html>
<meta charset="utf-8">
<head>
<link rel="stylesheet" href="./js/jquery/jquery-ui.css">
  <link rel="stylesheet" href="./js/jquery/basicStyle.css">
<style>

body {
  font: 10px sans-serif;
}

.axis path,
.axis line {
  fill: none;
  stroke: #000;
  shape-rendering: crispEdges;
}

.bar {
  fill: steelblue;
}

.x.axis path {
  display: none;
}

</style>
</head>
<body>

  <img src="./images/Cloudera_logo_rgb.png"/>
  <table width="100%" id="tbc"><tr><td>
  <a href="/fe/home.jsp">Home</a> > <a href="/fe/listRules.jsp">List Rules</a> > Create Rule
  </td></tr></table>

  <%
  String ruleId = request.getParameter("ruleId");
  String sourcePort = request.getParameter("sourcePort");
  String destinationPort = request.getParameter("destinationPort");
  String destinationIp = request.getParameter("destinationIp");

  %>

  <%
    if (ruleId != null && ruleId != "null") {

      HBaseService service = new HBaseService();
      RulePojo rule = new RulePojo(ruleId, sourcePort, destinationPort, destinationIp);
      service.putRule(rule);

  %>
        <TABLE WIDTH="80%" id="t01">
          <TR>
            <th style="vertical-align:top;">
                Field
            </th>
            <th style="vertical-align:top;">
                Value
            </th>
          </tr>
          <tr>
            <td>Rule ID</td>
            <td><%= ruleId%></td>
          </tr>
          <tr>
            <td>Source Port</td>
            <td><%= sourcePort%></td>
          </tr>
          <tr>
            <td>destinationPort</td>
            <td><%= destinationPort%></td>
          </tr>
          <tr>
            <td>destination IpAddress</td>
            <td><%= destinationIp%></td>
          </tr>
        </table>

        <a href="listRules.jsp">Return to Rules List Page</a>

  <%
    } else {
  %>


  <form action="createRulePage.jsp">
    <TABLE WIDTH="80%" id="t01">
      <TR>
        <th style="vertical-align:top;">
            Field
        </th>
        <th style="vertical-align:top;">
            Value
        </th>
      </tr>
      <tr>
        <td>Rule ID</td>
        <td><input type="text" name="ruleId" id="ruleId"></td>
      </tr>
      <tr>
        <td>Source Port</td>
        <td><input type="text" name="sourcePort" id="sourcePort"></td>
      </tr>
      <tr>
        <td>destinationPort</td>
        <td><input type="text" name="destinationPort" id="destinationPort"></td>
      </tr>
      <tr>
        <td>destination IpAddress</td>
        <td><input type="text" name="destinationIp" id="destinationIp"></td>
      </tr>
    </table>

    <input type="submit" value="Create Rule">
  </form>
<% } %>
</body>
</html>