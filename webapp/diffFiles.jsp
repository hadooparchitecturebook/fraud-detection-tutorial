<%@ page import="com.cloudera.sa.node360.service.HBaseService" %>
<%@ page import="java.text.*" %>
<%@ page import="com.cloudera.sa.node360.model.*" %>

<%
HBaseService service = new HBaseService();
SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy.HH.mm.ss");

String node1 = request.getParameter("node1");
String group1 = request.getParameter("grp1");
long sTime1 = dateFormat.parse(request.getParameter("sTime1")).getTime();

EventPojo event1Pretty = service.getWebPrettyClosestFileEvent(node1, sTime1, group1);
EventPojo event1 = service.getClosestFileEvent(node1, sTime1, group1);

String node2 = request.getParameter("node2");
String group2 = request.getParameter("grp2");
long sTime2 = dateFormat.parse(request.getParameter("sTime2")).getTime();

EventPojo event2Pretty = service.getWebPrettyClosestFileEvent(node2, sTime2, group2);
EventPojo event2 = service.getClosestFileEvent(node2, sTime2, group2);

String file1Display = "<tr><td>" + event1Pretty.getNewFile() + "</td></tr>";
String file2Display = "<tr><td>" + event2Pretty.getNewFile() + "</td></tr>";

%>

<HTML>
<HEAD>
  <TITLE>File Differences</TITLE>
    <link rel="stylesheet" href="./js/jquery/jquery-ui.css">
    <link rel="stylesheet" href="./js/jquery/basicStyle.css">
    <script src="./js/jquery/jquery-1.11.3.js"></script>
    <script src="./js/jquery/jquery-ui.js"></script>
    <SCRIPT TYPE="text/javascript" LANGUAGE="JavaScript" SRC="./js/diff_match_patch.js"></SCRIPT>
</HEAD>
<BODY>

<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a> > <a href="/fe/nodeHome.jsp?node=<%= node1%>"><%= node1%></a> > <a href="/fe/listFiles.jsp?node=<%= node1%>&grp=<%= group1%>&sTime=<%= request.getParameter("sTime1")%>"><%= group1%></a> > File Differences
</td></tr></table>

<H3>Compare File Content</H3>

<SCRIPT TYPE="text/javascript" LANGUAGE="JavaScript">
var dmp = new diff_match_patch();

function launch() {
  var text1 = document.getElementById('text1').value;
  var text2 = document.getElementById('text2').value;
  dmp.Diff_Timeout = parseFloat(document.getElementById('timeout').value);

  var ms_start = (new Date()).getTime();
  var d = dmp.diff_main(text1, text2);
  var ms_end = (new Date()).getTime();

  dmp.diff_cleanupSemantic(d);

  var ds = dmp.diff_prettyHtml(d);
  document.getElementById('outputdiv').innerHTML = ds;
}
</SCRIPT>

<FORM action="#" onsubmit="return false">

<TEXTAREA ID="text1" style="display:none;"><%= event1.getNewFile()%></TEXTAREA>

<TEXTAREA ID="text2" style="display:none;"><%= event2.getNewFile()%></TEXTAREA>
<INPUT TYPE="hidden" SIZE=3 MAXLENGTH=5 VALUE="1" ID="timeout">
</FORM>

<TABLE WIDTH="100%" id="t01">
  <TR>
    <th WIDTH="30%" style="vertical-align:top;">
        <%= event1.getNode()%>: </br> <%= event1.getMeta()%>
    </th>
    <th WIDTH="30%" style="vertical-align:top;  background-color: 4d4d4d;">
        <%= event2.getNode()%>: </br> <%= event2.getMeta()%>
    </th>
    <th WIDTH="30%" style="vertical-align:top;  background-color: 4d4d4d;">
        Diff
    </th>
  </tr>
<tr>
  <td style="vertical-align:top;">
    <TABLE WIDTH="100%" id="t01">
      <%= file1Display%>
    </table>
  </TD>

  <TD WIDTH="30%" style="vertical-align:top;">
    <TABLE WIDTH="100%" id="t01">
      <%= file2Display%>
    </table>
  </TD>

  <td WIDTH="30%" style="vertical-align:top;">
    <DIV ID="outputdiv" style="background: #ffffff;"></DIV>
  </td>
</TR></TABLE>


<script>
launch()
</script>

</BODY>
</HTML>
