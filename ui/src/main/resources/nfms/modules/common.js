define([ "message-bus" ], function(bus) {

   bus.listen("modules-loaded", function() {
      bus.send("show-window", [ "developers" ]);

      bus.send("ui-element:create", [ {
         "div" : "lblUserName",
         "parentDiv" : null,
         "type" : "div",
         "html" : "No identificado"
      } ]);

      bus.listen("set-user", function(e, userName) {
         bus.send("ui-set-content", [ {
            "div" : "lblUserName",
            "html" : "Hola " + userName
         } ]);
      });
   });

});