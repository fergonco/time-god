define([ "d3", "message-bus" ], function(d3, bus) {
   var span = d3.select("body").append("span").html("No identificado");
   bus.listen("set-user", function(e, userName) {
      span.html("Hola " + userName);
   });

   bus.listen("modules-loaded", function() {
      bus.send("show-window", [ "developers" ]);
   });

});