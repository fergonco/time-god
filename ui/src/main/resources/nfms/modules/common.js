define([ "d3", "message-bus" ], function(d3, bus) {
   var span = d3.select("body").append("span").html("No identificado");
   bus.listen("set-user", function(e, userName) {
      span.html("Hola " + userName);
   });

   bus.listen("modules-loaded", function() {
      bus.send("show-window", [ "developers" ]);
      // bus.send("add-developer", "fergonco");
      // bus.send("add-poker", {
      // name : "foo",
      // tasks : []
      // });
      //
      // bus.send("set-user", "fergonco");
      // bus.send("show-window", [ "pokers" ]);
   });

});