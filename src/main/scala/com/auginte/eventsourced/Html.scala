package com.auginte.eventsourced

object Html {
  val consumer =
    """
      |<html>
      |<head>
      |    <title>Stream example</title>
      |
      |    <!-- Inspired by http://www.html5rocks.com/en/tutorials/eventsource/basics/ -->
      |
      |    <script type="text/javascript">
      |        var source = new EventSource('/stream');
      |
      |        source.addEventListener('message', function(e) {
      |          var parsed = JSON.parse(e.data);
      |          console.log(parsed, e);
      |        }, false);
      |
      |        source.addEventListener('open', function(e) {
      |          console.log("OPEN", e)
      |        }, false);
      |
      |        source.addEventListener('error', function(e) {
      |          if (e.readyState == EventSource.CLOSED) {
      |            console.log("CLOSED", e)
      |          } else {
      |            console.log("ERROR", e)
      |          }
      |        }, false);
      |    </script>
      |</head>
      |<body>
      |
      |</body>
      |</html>
    """.stripMargin
}
