<!DOCTYPE html>
<meta charset="utf-8">
<link rel="stylesheet" href="./js/jquery/jquery-ui.css">
  <link rel="stylesheet" href="./js/jquery/basicStyle.css">
<style>

.node {
  stroke: #222;
  stroke-width: 1.5px;
}

.link {
  stroke: #ccc;
}

.node text {
  pointer-events: none;
  font: 12px sans-serif;
  background-color:#bbb;
}

</style>
<body>

<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a> > <a href="/fe/nodeHome.jsp?node=<%= request.getParameter("node")%>"><%= request.getParameter("node")%></a> > <%= request.getParameter("grp")%>
</td></tr></table>


<script src="http://d3js.org/d3.v3.min.js"></script>
<script>

var width = 960,
    height = 700;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-120)
    .linkDistance(120)
    .size([width, height]);

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

d3.json("/be/nodeGraph/nodeGraph.json?node=<%= request.getParameter("node") %>&grp=<%= request.getParameter("grp") %>&sTime=1.22.2015.11.11.11&eTime=12.22.2015.11.11.11", function(error, graph) {


  force
      .nodes(graph.nodes)
      .links(graph.links)
      .start();

  var link = svg.selectAll(".link")
      .data(graph.links)
    .enter().append("line")
      .attr("class", "link")
      .style("stroke-width", function(d) { return Math.sqrt(d.value); });
/*
  var node = svg.selectAll(".node")
      .data(graph.nodes)
    .enter().append("circle") //circle
      .attr("class", "node")
      .attr("r", function(d){ return (d.group * 2) + 8; })
      .style("fill", function(d) { return color(d.group); })
      .call(force.drag);

  node.append("title")
      .text(function(d) { return d.name; });

        force.on("tick", function() {
          link.attr("x1", function(d) { return d.source.x; })
              .attr("y1", function(d) { return d.source.y; })
              .attr("x2", function(d) { return d.target.x; })
              .attr("y2", function(d) { return d.target.y; });

node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });
*/

//
var node = svg.selectAll(".node")
      .data(graph.nodes)
    .enter().append("g")
      .attr("class", "node")
      .call(force.drag);

  node.append("circle") //circle
             .attr("class", "node")
             .attr("r", function(d){ return (d.group * 2) + 8; })
             .style("fill", function(d) { return color(d.group); })
             .call(force.drag);

  node.append("text")
      .attr("dx", 12)
      .attr("dy", -10)
      .text(function(d) { return d.name });

force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });


  });
});

</script>

<a href="http://localhost:8082/be/nodeGraph/nodeGraph.json?node=<%= request.getParameter("node") %>&grp=<%= request.getParameter("grp") %>&sTime=1.22.2015.11.11.11&eTime=12.22.2015.11.11.11">Download JSON</a>