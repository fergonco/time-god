define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var userName = null;
   var poker = null;
   var currentView = null;

   var container = d3.select("body").append("div").attr("id", "poker-detail");
   container.style("display", "none");

   var spnTitle = container.append("h1").html("Tareas de ").append("span");

   var divButtons = container.append("div");
   divButtons//
   .append("span")//
   .attr("class", "span-button")//
   .html("volver")//
   .on("click", function() {
      bus.send("show-window", [ "pokers" ]);
   });

   var INDIVIDUAL = "individual";
   var COMMON = "common";
   var PROGRESS = "progress";
   var views = {};
   views[INDIVIDUAL] = function(selection) {
      selection.append("input")//
      .attr("type", "text")//
      .attr("value", function(task) {
         var estimation = task.estimations[userName];
         return estimation ? estimation : "";
      })//
      .on("change", function(task) {
         bus.send("change-task-user-credits", [ userName, task.id, this.value ]);
      });
      selection//
      .append("span")//
      .html("Your estimation:");
   }

   views[COMMON] = function(selection) {
      selection.append(function(task) {
         var div = document.createElement("div");
         var estimationSelection = d3.select(div).selectAll(".estimation-summary").data(Object.keys(task.estimations));
         estimationSelection.exit().remove();
         estimationSelection.enter().append("div");
         estimationSelection.attr("class", "estimation-summary");
         estimationSelection.html(function(dev) {
            return dev + ":" + task.estimations[dev]
         });
         return div;
      });
      selection.append("input")//
      .attr("type", "text")//
      .attr("value", function(task) {
         return task.commonEstimation;
      })//
      .on("change", function(task) {
         var taxonomyProcessedListener = function(e, type, keywords) {
            if (type == COMMON) {
               bus.stopListen("taxonomy-processed", taxonomyProcessedListener);
               wsbus.send("change-task-keywords", {
                  "taskId" : task.id,
                  "keywords" : keywords
               });
            }
         };
         bus.listen("taxonomy-processed", taxonomyProcessedListener);

         bus.send("show-taxonomy", [ this, COMMON ]);
         bus.send("change-task-common-credits", [ task.id, this.value ]);
      });
      selection//
      .append("span")//
      .html("Valoración común:")//
      .on("click", function() {
         bus.send("show-pokers");
      });
   }

   views[PROGRESS] = function(selection) {

      function getTotalTime(task) {
         var acum = 0;
         for (var i = 0; i < task.timeSegments.length; i++) {
            var timeSegment = task.timeSegments[i];
            acum += timeSegment.end - timeSegment.start;
         }
         return acum / (1000 * 60 * 60);
      }

      selection//
      .append("progress")//
      .attr("title", function(task) {
         return getTotalTime(task) + " de " + task.commonEstimation;
      })//
      .attr("max", "100")//
      .attr("value", function(task) {
         var acum = getTotalTime(task);
         return 100 * acum / task.commonEstimation;
      });

      selection//
      .append("span")//
      .attr("class", "span-button")//
      .html("reporte horas")//
      .on("click", function(task) {
         bus.send("report-time", [ task ]);
      });

   }

   var cmbViews = divButtons//
   .append("select")//
   .on("change", function() {
      currentView = views[this.value];
      assert(currentView, "no view selected");
      list.refresh(poker.tasks);
   });
   cmbViews.append("option").attr("value", INDIVIDUAL).html("estimación individual");
   cmbViews.append("option").attr("value", COMMON).html("puesta en común");
   cmbViews.append("option").attr("value", PROGRESS).html("progreso del proyecto");

   var list = editableList.create(container);
   list.entryClassName("task-entry");

   list.add(function(text) {
      bus.send("add-task-to-poker", [ poker.name, {
         "name" : text,
         "estimations" : {},
         "commonEstimation" : null
      } ]);
   });

   list.remove(function(task) {
      bus.send("remove-task", [ task.id ]);
   });

   list.select(function(d) {
   });

   list.renderer(function(d) {
      var ret = d.name;
      if (d.keywords) {
         ret += "  (";
         for (var i = 0; i < d.keywords.length; i++) {
            ret += d.keywords[i] + ",";

         }
         ret = ret.substring(0, ret.length - 1) + ")";
      }
      return ret;
   });

   list.postProcess(function(selection) {
      currentView(selection);
   });

   function estimations(tasks) {
      for (var i = 0; i < tasks.length; i++) {
         var task = tasks[i];
         if (Object.keys(task.estimations).length > 0) {
            return true;
         }
      }

      return false;
   }

   function commonEstimation(tasks) {
      for (var i = 0; i < tasks.length; i++) {
         var task = tasks[i];
         if (task.commonEstimation == null) {
            return false;
         }
      }

      return true;
   }

   bus.listen("show-window", function(e, window) {
      if (window == "poker") {
         container.style("display", "block");
         currentView = null;
      } else {
         container.style("display", "none");
      }
   });

   bus.listen("selected-poker", function(e, poker) {
      wsbus.send("get-poker", poker.name);
   });

   bus.listen("updated-poker", function(e, newPoker) {
      poker = newPoker;
      spnTitle.html(poker.name);
      if (currentView == null) {
         if (!estimations(poker.tasks)) {
            currentView = views[INDIVIDUAL];
         } else if (!commonEstimation(poker.tasks)) {
            currentView = views[COMMON];
         } else {
            currentView = views[PROGRESS];
         }
      }
      list.refresh(poker.tasks);
   });
   bus.listen("set-user", function(e, newUserName) {
      userName = newUserName;
   });

});