define([ "d3", "message-bus", "editableList" ], function(d3, bus, editableList) {

   // TODO hardcoded user
   var userName = "fergonco";
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

   editableList.buildAdd(container, function(text) {
      bus.send("add-task-to-poker", [ pokerName, {
         "name" : text,
         "estimations" : {}
      } ]);
   });

   function refresh(tasks) {
      editableList.refresh(container, tasks, "task-entry", {
         nameGetter : function(t) {
            return t.name;
         },
         selectionPostprocess : function(selection) {
            selection.append("span")//
            .attr("class", "span-button")//
            .html("borrar")//
            .on("click", function(task) {
               // TODO remove
            });
            selection//
            .append("span")//
            .html("Your estimation:")//
            .on("click", function() {
               bus.send("show-pokers");
            });
            selection.append("input")//
            .attr("type", "text")//
            .attr("value", function(task) {
               var estimation = task.estimations[userName];
               return estimation ? estimation : "";
            })//
            .on("change", function(task) {
               bus.send("change-task-credits", [ userName, pokerName, task.name, this.value ]);
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