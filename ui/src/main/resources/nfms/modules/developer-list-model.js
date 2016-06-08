define([ "developer-list-ui", "message-bus", "websocket-bus" ], function(ui, bus, wsbus) {

   ui.entryClassName("dev-entry");
   
   ui.addDeveloper(function(text) {
      bus.send("add-developer", [ {
         "name" : text
      } ]);
   });

   ui.removeDeveloper(function(d) {
      bus.send("remove-developer", [ d.name ]);
      d3.event.stopPropagation();
   });

   ui.selectDeveloper(function(d) {
      bus.send("set-user", d.name);
      bus.send("show-window", [ "pokers" ]);
   });

   ui.renderer(function(d) {
      return d.name;
   });

   bus.listen("show-window", function(e, window) {
      if (window == "developers") {
         ui.show();
         wsbus.send("get-developers");
      } else {
         ui.hide();
      }
   });

   bus.listen("updated-developer-list", function(e, devList) {
      ui.refresh(devList);
   });
});