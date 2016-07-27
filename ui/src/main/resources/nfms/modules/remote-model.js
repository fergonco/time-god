define([ "message-bus", "websocket-bus" ], function(bus, wsbus) {

   bus.listen("add-developer", function(e, d) {
      wsbus.send("add-developer", d);
   });

   bus.listen("remove-developer", function(e, name) {
      wsbus.send("remove-developer", name);
   });

   bus.listen("add-poker", function(e, p) {
      wsbus.send("add-poker", p);
   });

   bus.listen("remove-poker", function(e, name) {
      wsbus.send("remove-poker", name);
   });

});