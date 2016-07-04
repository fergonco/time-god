define([ "message-bus", "websocket-bus", "editableList" ], function(bus, wsbus, editableList) {

   var developerListId = "developer-list";

   bus.send("ui-element:create", {
      "type" : "div",
      "div" : developerListId,
      "parentDiv" : null,
      "html" : "<h1>devélopers devélopers devélopers</h1>"
   });

   bus.send("ui-hide", developerListId);
   var list = editableList.create(developerListId);

   list.entryClassName("dev-entry");

   list.add(function(text) {
      bus.send("add-developer", [ {
         "name" : text
      } ]);
   });

   list.remove(function(d) {
      bus.send("remove-developer", [ d.name ]);
   });

   list.select(function(d) {
      bus.send("set-user", d.name);
      bus.send("show-window", [ "pokers" ]);
   });

   list.renderer(function(d) {
      return d.name;
   });

   bus.listen("show-window", function(e, window) {
      if (window == "developers") {
         bus.send("ui-show", developerListId);
         wsbus.send("get-developers");
      } else {
         bus.send("ui-hide", developerListId);
      }
   });

   bus.listen("updated-developer-list", function(e, devList) {
      list.refresh(devList);
   });
});