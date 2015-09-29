<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Home page</title>
  <link rel="stylesheet" href="./js/jquery/basicStyle.css">
  <link rel="stylesheet" href="./js/jquery/jquery-ui.css">
  <script src="./js/jquery/jquery-1.11.3.js"></script>
  <script src="./js/jquery/jquery-ui.js"></script>

  <style>
  .ui-autocomplete-loading {
    background: white url("images/ui-anim_basic_16x16.gif") right center no-repeat;
  }
  </style>
  <script>
  $(function() {
    function split( val ) {
      return val.split( /,\s*/ );
    }
    function extractLast( term ) {
      return split( term ).pop();
    }

    $( "#node" )
      .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB &&
            $( this ).autocomplete( "instance" ).menu.active ) {
          event.preventDefault();
        }
      })
      .autocomplete({
        source: function( request, response ) {
          $.getJSON( "/be/auto/search.php", {
            term: extractLast( request.term )
          }, response );
        },
        search: function() {
          // custom minLength
          var term = extractLast( this.value );
          if ( term.length < 2 ) {
            return false;
          }
        },
        focus: function() {
          // prevent value inserted on focus
          return false;
        },
        select: function( event, ui ) {
          var terms = split( this.value );
          // remove the current input
          terms.pop();
          // add the selected item
          terms.push( ui.item.value );
          // add placeholder to get the comma-and-space at the end
          //terms.push( "" );
          this.value = terms;//.join( ", " );
          return false;
        }
      });
  });
  </script>
</head>
<body>

<img src="./images/Cloudera_logo_rgb.png"/>
<table width="100%" id="tbc"><tr><td>
<a href="/fe/home.jsp">Home</a>
</td></tr></table>

<br/>
<H3>Single Node View</H3>
<form action="nodeHome.jsp">
  <div class="ui-widget">
    Node:
    <input name="node" id="node" size="50">
    <input type="submit" value="Submit">
  </div>
</form>

<H3>Rule & Notifications</H3>
<a href="listRules.jsp">List of Active Rules</a>
<a href="listNotifications.jsp">List of Most Resent Notifications</a>

</body>
</html>