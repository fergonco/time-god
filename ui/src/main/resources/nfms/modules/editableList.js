define([ "message-bus", "ui-values", "d3" ], function(bus, uiValues) {

   function create(containerId) {
      var id = new Date().getTime();
      var add = null;
      var remove = null;
      var select = null;
      var renderer = null;
      var refresh = null;
      var entryClassName = null;
      var postProcess = null;

      bus.send("ui-input-field:create", {
         "div" : "txtAdd-" + id,
         "parentDiv" : containerId
      });

      bus.send("ui-button:create", {
         "div" : "editableList-btn-add-" + id,
         "parentDiv" : containerId,
         "text" : "Añadir",
         "sendEventName" : "editableList-btn-add-" + id
      });
      bus.listen("editableList-btn-add-" + id, function() {
         add(uiValues.get("txtAdd-" + id));
         uiValues.set("txtAdd-" + id, "");
      });

      var instance = {
         add : function(listener) {
            add = listener;
         },
         remove : function(listener) {
            remove = listener;
         },
         select : function(listener) {
            select = listener;
         },
         renderer : function(listener) {
            renderer = listener;
         },
         refresh : function(list) {
            var selection = d3.select("#" + containerId).selectAll("." + entryClassName).data(list);
            selection.exit().remove();
            selection.enter().append("div");
            selection.attr("class", entryClassName);
            selection.html(function(d) {
               return renderer(d);
            });
            selection.append("span")//
            .on("click", function(d) {
               bus.send("jsdialogs.confirm", [ {
                  "message" : "Are you sure you want to remove " + renderer(d) + "?",
                  "okAction" : function() {
                     remove(d);
                  }
               } ]);
            })//
            .each(function(d, i) {
               bus.send("ui-button:create", {
                  "element" : this,
                  "text" : "borrar"
               });
            });

            selection.on("click", function(d) {
               select(d);
               d3.event.stopPropagation();
            });
            if (postProcess != null) {
               postProcess(selection);
            }
         },
         entryClassName : function(className) {
            entryClassName = className;
         },
         postProcess : function(postProcessFunction) {
            postProcess = postProcessFunction;
         }
      };

      return instance;
   }

   return {
      "create" : create,

   }

});