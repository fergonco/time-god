define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var container = d3.select("body").append("div").attr("id", "poker-list");
   container.style("display", "none");

   editableList.buildAdd(container, function(text) {
      bus.send("add-poker", [ {
         "name" : text,
         "tasks" : []
      } ]);
   });

   function refresh(pokerList) {
      editableList.refresh(container, pokerList, "poker-entry", {
         "nameGetter" : function(t) {
            // TODO mis hogos!
            return t.name;
         },
         "selectionPostprocess" : function(selection) {
            selection.append("span")//
            .attr("class", "span-button")//
            .html("borrar")//
            .on("click", function(p) {
               // TODO mis hogos!
               bus.send("remove-poker", [ p.name ]);
               d3.event.stopPropagation();
            });
            selection.on("click", function(d) {
               bus.send("show-window", [ "poker" ]);
               bus.send("selected-poker", d)
            });
         }
      });
   }

   bus.listen("show-window", function(e, window) {
      if (window == "pokers") {
         container.style("display", "block");
         wsbus.send("get-pokers");
      } else {
         container.style("display", "none");
      }
   });

   bus.listen("updated-poker-list", function(e, pokerList) {
      refresh(pokerList);
   });
});