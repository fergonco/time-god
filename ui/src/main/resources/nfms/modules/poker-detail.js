define([ "d3", "message-bus", "editableList", "poker-total-credits" ], function(d3, bus, editableList, pokerTotalCredits) {

   var pokerName;

   var container = d3.select("body").append("div").attr("id", "poker-detail");
   container.style("display", "none");

   var spnTitle = container.append("h1").html("Tareas de ").append("span");

   var divButtons = container.append("div");
   divButtons//
   .append("span")//
   .attr("class", "span-button")//
   .html("volver")//
   .on("click", function() {
      bus.send("show-pokers");
   });
   pokerTotalCredits.addButtons(divButtons);

   editableList.buildAdd(container, function(text) {
      bus.send("add-task-to-poker", [ pokerName, text ]);
   });

   function refresh(tasks) {
      editableList.refresh(container, tasks, "task-entry", {
         nameGetter : function(t) {
            return t;
         },
         selectionPostprocess : function(selection) {
            selection.append("span")//
            .attr("class", "span-button")//
            .html("borrar")//
            .on("click", function(p) {
               // TODO remove
            });
         }
      });
   }

   bus.listen("show-poker", function(e) {
      container.style("display", "block");
   });
   bus.listen("show-pokers", function() {
      container.style("display", "none");
   });

   bus.listen("updated-poker", function(e, poker) {
      spnTitle.html(poker.name);
      pokerName = poker.name;
      refresh(poker.tasks);
   });

});