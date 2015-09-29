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
<a href="/fe/home.jsp">Home</a> > Notification List
</td></tr></table>

<%
HBaseService service = new HBaseService();
List<NotificationPojo> notificationPojoList = service.getLastNNotifications(100);
SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm/ss");
%>

  <H3>Notification List</H3>
  <TABLE WIDTH="100%" id="t01">
   <TR>
     <th>Time</th>
     <th>Notification</th>
     <th>Time to Detection</th>
   </tr>

<%
  for (NotificationPojo pojo: notificationPojoList) {

%>
   <TR>
     <td><%= simpleDateFormat.format(new Date(pojo.getTimeOfNotification()))%></td>
     <td><%= pojo.getNotificationMessage()%></td>
     <td><%= pojo.getTimeToAlert()%></td>
   </TR>

<%
  }
%>
  </table>

 </body>
</head>