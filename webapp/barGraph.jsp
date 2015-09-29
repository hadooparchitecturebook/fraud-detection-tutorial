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
<a href="/fe/home.jsp">Home</a> > <a href="/fe/nodeHome.jsp?node=<%= request.getParameter("node")%>"><%= request.getParameter("node")%></a> > <%= request.getParameter("grp")%>
</td></tr></table>

<script src="http://d3js.org/d3.v3.min.js"></script>
<script>

var margin = {top: 20, right: 120, bottom: 30, left: 80},
    width = 960 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var parseDate = d3.time.format("%m%d%H%M%S").parse;

var x = d3.time.scale()
    .range([0, width]);

//var x = d3.scale.ordinal()
//    .rangeRoundBands([0, width], .1);

var y = d3.scale.linear()
    .rangeRound([height, 0]);

var color = d3.scale.category10();

var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .tickFormat(d3.format(".2s"));

var svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

d3.tsv("/be/graph.tsv?node=<%= request.getParameter("node") %>&grp=<%= request.getParameter("grp") %>&sTime=1.22.2015.11.11.11&eTime=12.22.2015.11.11.11", function(error, data) {
  color.domain(d3.keys(data[0]).filter(function(key) { return key !== "date"; }));

  data.forEach(function(d) {
    d.date = parseDate(d.date);
  });

  data.forEach(function(d) {
    var y0 = 0;
    d.ages = color.domain().map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
    d.total = d.ages[d.ages.length - 1].y1;
  });

  data.sort(function(a, b) { return b.total - a.total; });

  x.domain(d3.extent(data, function(d) { return d.date; }));
  //x.domain(data.map(function(d) { return d.date; }));
  y.domain([0, d3.max(data, function(d) { return d.total; })]);

  svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis);

  svg.append("g")
      .attr("class", "y axis")
      .call(yAxis)
    .append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", ".71em")
      .style("text-anchor", "end")
      .text("Population");

  var date = svg.selectAll(".date")
      .data(data)
    .enter().append("g")
      .attr("class", "g")
      .attr("transform", function(d) { return "translate(" + x(d.date) + ",0)"; });

  date.selectAll("rect")
      .data(function(d) { return d.ages; })
    .enter().append("rect")
      .attr("width", 600/data.length) //x.rangeBand())
      .attr("y", function(d) { return y(d.y1); })
      .attr("height", function(d) { return y(d.y0) - y(d.y1); })
      .style("fill", function(d) { return color(d.name); });

  var legend = svg.selectAll(".legend")
      .data(color.domain().slice().reverse())
    .enter().append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate(100," + i * 30 + ")"; });

  legend.append("rect")
      .attr("x", width - 18)
      .attr("width", 18)
      .attr("height", 18)
      .style("fill", color)
      .style("stroke", "black");

  legend.append("text")
      .attr("x", width - 24)
      .attr("y", 9)
      .attr("dy", ".35em")
      .style("text-anchor", "end")
      .text(function(d) { return d; });

});

</script>