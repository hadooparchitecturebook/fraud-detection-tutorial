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

<%
HBaseService service = new HBaseService();
SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy.HH.mm.ss");
SimpleDateFormat prettyDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
String node = request.getParameter("node");

NodeStatusPojo nodeStatusPojo = service.getNodeStatus(node);
%>

<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a> > <%= node%>
</td></tr></table>

<table WIDTH="100%" ><tr><td valign="top">

<H3>CPU Usage</H3>
<TABLE WIDTH="100%" id="t01">
  <TR>
    <TH>Name</TH>
    <TH>Value</TH>
    <TH>Max</TH>
    <TH>Percentage</TH>
  </TR>
  <TR>
    <Td>User CPU</Td>
    <Td><div id="cpuUserDataField"></div></Td>
    <Td>100</Td>
    <Td>
      <div id="progressbarUserCPU"></div>
    </Td>
  </TR>
  <TR>
    <Td>System CPU</Td>
    <Td><div id="cpuSysDataField"></div></Td>
    <Td>100</Td>
    <Td><div id="progressbarSysCPU"></div></Td>
  </TR>
  <TR>
    <Td>Idle CPU</Td>
    <Td><div id="cpuIdleDataField"></div></Td>
    <Td>100</Td>
    <Td><div id="progressbarIdleCPU"></div></Td>
  </TR>
</table>

<H3>Memory Usage</H3>
<TABLE WIDTH="100%" id="t01">
  <TR>
    <TH>Name</TH>
    <TH>Value</TH>
    <TH>Max</TH>
    <TH>Percentage</TH>
  </TR>
  <TR>
    <Td>Memory</Td>
    <Td><div id="memoryUsedDataField"></div></Td>
    <Td><div id="memoryUsedMaxDataField"></div></Td>
    <Td><div id="progressbarMemory"></div></Td>
  </TR>
  <TR>
    <Td>Swap</Td>
    <Td><div id="swapSwapDataField"></div></Td>
    <Td><div id="swapSwapMaxDataField"></div></Td>
    <Td><div id="progressbarSwap"></div></Td>
  </TR>
</table>

<H3>Drill Down</H3>
<TABLE WIDTH="100%" id="t01">
  <TR>
    <TH>Type</TH>
    <TH>Field</TH>
    <TH>Action</TH>
  </TR>
  <TR>
    <TD>
      Charts
    </TD>
    <TD>
      <select name="chartSelect" id="chartSelect">
        <option value="cpu">Cpu Usage</option>
        <option value="memory">Memory Usage</option>
        <option value="drives">Disk Usage</option>
        <option value="graphNodes">Network Relations</option>
      </select>
    </TD>
    <TD>
      <button onclick="redirectToChart()">View Chart</button>
    </TD>
  </TR>
  <TR>
    <TD>
      File History
    </TD>
    <TD>
      <select name="chartFile" id="chartFile">
        <option value="host">/etc/host</option>
      </select>
    </TD>
    <TD>
      <button onclick="redirectToFileHistory()">View File Change History</button>
    </TD>
  </TR>
</TABLE>


<script>
  function redirectToChart() {
     var selectedChart = document.getElementById("chartSelect").value;
      if (selectedChart == "cpu") {
        window.location.href = "./barGraph.jsp?node=<%= node%>&grp=cpu";
      } else if (selectedChart == "memory") {
        window.location.href = "./lineGraph.jsp?node=<%= node%>&grp=memory";
      } else if (selectedChart == "drives") {
        window.location.href = "./lineGraph.jsp?node=<%= node%>&grp=drives";
      } else if (selectedChart == "graphNodes") {
         window.location.href = "./nodeGraph.jsp?node=<%= node%>&grp=netflow";
      }
  }

  function redirectToFileHistory() {
       var selectedChart = document.getElementById("chartFile").value;
        if (selectedChart == "host") {
          window.location.href = "./listFiles.jsp?node=<%= node%>&grp=/etc/hosts&sTime=12.22.2015.11.11.11";
        }
    }

  $(function() {
    $( "#progressbarUserCPU" ).progressbar({
      value: <%= nodeStatusPojo.getGroupSampleMap().get("cpu").get("User").getValue()%>
    });
  $( "#progressbarSysCPU" ).progressbar({
      value: <%= nodeStatusPojo.getGroupSampleMap().get("cpu").get("Sys").getValue()%>
    });
  $( "#progressbarIdleCPU" ).progressbar({
      value: <%= nodeStatusPojo.getGroupSampleMap().get("cpu").get("Idle").getValue()%>
    });
  $( "#progressbarMemory" ).progressbar({
      value: <%= (Double.parseDouble(nodeStatusPojo.getGroupSampleMap().get("memory").get("Used").getValue()) / Double.parseDouble(nodeStatusPojo.getGroupSampleMap().get("memory").get("Used").getMax())) * 100%>
    });
  $( "#progressbarSwap" ).progressbar({
      value: <%= (Double.parseDouble(nodeStatusPojo.getGroupSampleMap().get("swap").get("Swap").getValue()) / Double.parseDouble(nodeStatusPojo.getGroupSampleMap().get("swap").get("Swap").getMax())) * 100%>
    });
  });
</script>

</td><td>

<H3>Drives Usage</H3>
<TABLE WIDTH="100%" id="t01">
  <TR>
    <TH>Name</TH>
    <TH>Value</TH>
    <TH>Max</TH>
    <TH>Percentage</TH>
  </TR>
  <%
   Iterator<SamplePojo> samples =  nodeStatusPojo.getGroupSampleMap().get("drives").values().iterator();
   for (int i = 0; samples.hasNext(); i++) {
     SamplePojo sample = samples.next();
  %>
  <TR>
    <Td><%= sample.getKey()%></Td>
    <Td><div id="drives<%= sample.getKey().replaceAll("/","_")%>DataField"></div></Td>
    <Td><div id="drives<%= sample.getKey().replaceAll("/","_")%>MaxDataField"></div></Td>
    <Td><div id="progressbarDrives<%= sample.getKey().replaceAll("/","_")%>"></div></Td>
  </TR>
  <script>
    $(function() {
      $( "#progressbarDrives<%= sample.getKey().replaceAll("/","_")%>" ).progressbar({
        value: <%= Double.parseDouble(sample.getValue()) / Double.parseDouble(sample.getMax()) * 100%>
      });
    });
  </script>
  <%
   }
  %>
</table>

<div id="content"></div>

<script>
  setInterval(function(){
    updateData();
  }, 5000);

function updateData() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open( "GET", "/be/nodeDash/foo?node=<%= node%>" , false );
  xmlHttp.send( null );
  //document.getElementById("content").innerHTML = xmlHttp.responseText;

  var lines = xmlHttp.responseText.split('\n');
  for (i = 0; i < lines.length; i++) {
    var parts = lines[i].split(',');
    if (parts[0] == "cpu") {
      document.getElementById("cpu" + parts[1] + "DataField").innerHTML = parts[2];

        $( "#progressbar" + parts[1] + "CPU" ).progressbar({
                value: parseInt(parts[2])});

    } else if (parts[0] == "memory") {
      document.getElementById("memory" + parts[1] + "DataField").innerHTML = parts[2];
      document.getElementById("memory" + parts[1] + "MaxDataField").innerHTML = parts[3];

      $( "#progressbarMemory" ).progressbar({
                          value: (parseInt(parts[2]) / parseInt(parts[3])) * 100});

    } else if (parts[0] == "swap") {
      document.getElementById("swap" + parts[1] + "DataField").innerHTML = parts[2];
      document.getElementById("swap" + parts[1] + "MaxDataField").innerHTML = parts[3];

      $( "#progressbarSwap" ).progressbar({
                                    value: (parseInt(parts[2]) / parseInt(parts[3])) * 100});

    } else if (parts[0] == "drives") {
      document.getElementById("drives" + parts[1].replace('/', '_').replace('/', '_') + "DataField").innerHTML = parts[2];
      document.getElementById("drives" + parts[1].replace('/', '_').replace('/', '_') + "MaxDataField").innerHTML = parts[3];

      $( "#progressbarDrives" + parts[1].replace('/', '_').replace('/', '_')).progressbar({
                                              value: (parseInt(parts[2]) / parseInt(parts[3])) * 100});
    }
  }
}

updateData();
</script>

</td></tr></table>

