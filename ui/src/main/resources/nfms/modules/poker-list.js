define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var pokerListId = "poker-list";

   bus.send("ui-element:create", {
      "type" : "div",
      "div" : pokerListId,
      "parentDiv" : null,
      "html" : "<h1>Proyectos</h1>"
   });
   bus.send("ui-hide", pokerListId);

   var list = editableList.create(pokerListId);

   list.entryClassName("poker-entry");

   list.add(function(text) {
      bus.send("add-poker", [ {
         "name" : text,
         "tasks" : []
      } ]);
   });

   list.remove(function(p) {
      bus.send("remove-poker", [ p.name ]);
   });

   list.select(function(d) {
      bus.send("show-window", [ "poker" ]);
      bus.send("selected-poker", d)
   });

   list.renderer(function(d) {
      return d.name;
   });

   list.postProcess(function(selection) {
      selection//
      .append("span")//
      .on("click", function(poker) {
         var taxonomyProcessedListener = function(e, type, keywords) {
            if (type == "poker") {
               bus.stopListen("taxonomy-processed", taxonomyProcessedListener);
               wsbus.send("change-poker-keywords", {
                  "pokerName" : poker.name,
                  "keywords" : keywords
               });
            }
         };
         bus.listen("taxonomy-processed", taxonomyProcessedListener);

         bus.send("show-taxonomy", [ this, "poker" ]);
         d3.event.stopPropagation();
      })//
      .each(function(d, i) {
         bus.send("ui-button:create", {
            "element" : this,
            "text" : "establecer keywords"
         });
      });

      selection.attr("title", function(d) {
         var ret = "";
         if (d.keywords) {
            for (var i = 0; i < d.keywords.length; i++) {
               ret += d.keywords[i] + ",";

            }
            ret = ret.substring(0, ret.length - 1);
         }
         return ret;
      });

   });

   bus.listen("show-window", function(e, window) {
      if (window == "pokers") {
         bus.send("ui-show", pokerListId);
         wsbus.send("get-pokers");
      } else {
         bus.send("ui-hide", pokerListId);
      }
   });

   bus.listen("updated-poker-list", function(e, pokerList) {
      list.refresh(pokerList);
   });
});