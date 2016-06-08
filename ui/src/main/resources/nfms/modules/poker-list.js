define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var container = d3.select("body").append("div").attr("id", "poker-list");
   container.style("display", "none");

   container.append("h1").html("Proyectos");

   var list = editableList.create(container);

   list.entryClassName("poker-entry");

   list.add(function(text) {
      bus.send("add-poker", [ {
         "name" : text,
         "tasks" : []
      } ]);
   });

   list.remove(function(p) {
      bus.send("remove-poker", [ p.name ]);
      d3.event.stopPropagation();
   });

   list.select(function(d) {
      bus.send("show-window", [ "poker" ]);
      bus.send("selected-poker", d)
   });

   list.renderer(function(d) {
      return d.name;
   });

   bus.listen("show-window", function(e, window) {
      if (window == "pokers") {
         container.style("display", "block");
         wsbus.send("get-pokers");
      } else {
         container.style("display", "none");
      }
   });

   bus.listen("updated-poker-list", function(e, pokerList) {
      list.refresh(pokerList);
   });
});