define([ "d3", "message-bus", "editableList" ], function(d3, bus, editableList) {

   var container = d3.select("body").append("div").attr("id", "developer-list");
   container.style("display", "none");

   var spnTitle = container.append("h1").html("devélopers devélopers devélopers");

   editableList.buildAdd(container, function(text) {
      bus.send("add-developer", [ {
         "name" : text
      } ]);
   });

   function refresh(devList) {
      editableList.refresh(container, devList, "dev-entry", {
         "nameGetter" : function(d) {
            // TODO mis hogos!
            return d.name;
         },
         "selectionPostprocess" : function(selection) {
            selection.append("span")//
            .attr("class", "span-button")//
            .html("borrar")//
            .on("click", function(d) {
               // TODO mis hogos!
               bus.send("remove-developer", [ d.name ]);
               d3.event.stopPropagation();
            });
            selection.on("click", function(d) {
               bus.send("set-user", d.name);
               bus.send("show-window", [ "pokers" ]);
            });
         }
      });
   }

   bus.listen("show-window", function(e, window) {
      if (window == "developers") {
         container.style("display", "block");
      } else {
         container.style("display", "none");
      }
   });

   bus.listen("updated-developer-list", function(e, devList) {
      refresh(devList);
   });
});