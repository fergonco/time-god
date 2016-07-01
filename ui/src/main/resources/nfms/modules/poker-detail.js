define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var userName = null;
   var poker = null;
   var currentView = null;

   var pokerDetailId = "poker-detail";

   bus.send("ui-element:create", {
      "type" : "div",
      "div" : pokerDetailId,
      "parentDiv" : null
   });
   bus.send("ui-hide", pokerDetailId);

   bus.send("ui-element:create", {
      "type" : "span",
      "div" : "poker-detail-title",
      "parentDiv" : pokerDetailId
   });
   bus.listen("totalCreditsUpdated", function(e, value) {
      wsbus.send("change-poker-totalCredits", {
         "pokerName" : poker.name,
         "totalCredits" : value
      });
   });
   bus.send("ui-input-field:create", {
      "div" : "txt-project-totalCredits",
      "parentDiv" : pokerDetailId,
      "text" : "Duración del proyecto: ",
      "changeEventName" : "totalCreditsUpdated"
   });

   bus.send("ui-progress:create", {
      "div" : "progress-project-estimated",
      "parentDiv" : pokerDetailId,
      "text" : "Total estimación de tareas: ",
      "tooltip" : function() {
         if (poker != null) {
            return getTotalEstimation(poker) + " de " + poker.totalCredits;
         } else {
            return null;
         }
      }
   });

   bus.send("ui-progress:create", {
      "div" : "progress-project-real",
      "parentDiv" : pokerDetailId,
      "text" : "Total consumido: ",
      "tooltip" : function() {
         if (poker != null) {
            return getTotalReported(poker) + " de " + poker.totalCredits;
         } else {
            return null;
         }
      }
   });

   var divButtonsId = "div-buttons";
   bus.send("ui-element:create", {
      "type" : "div",
      "div" : divButtonsId,
      "parentDiv" : pokerDetailId
   });

   bus.send("ui-button:create", {
      "div" : "poker-detail-back",
      "parentDiv" : divButtonsId,
      "text" : "Volver",
      "sendEventName" : "show-window",
      "sendEventMessage" : "pokers"
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
      .on("click", function(task) {
         d3.event.stopPropagation();
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
      .on("click", function(task) {
         d3.event.stopPropagation();
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
      .html("Valoración común:");
   }

   views[PROGRESS] = function(selection) {

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
      .each(function(d) {
         bus.send("ui-button:create", {
            "element" : this,
            "text" : "Reporte horas",
            "sendEventName" : "report-time",
            "sendEventMessage" : d
         });
      });

   }
   function getTotalTime(task) {
      var acum = 0;
      if (task.timeSegments) {
         for (var i = 0; i < task.timeSegments.length; i++) {
            var timeSegment = task.timeSegments[i];
            acum += timeSegment.end - timeSegment.start;
         }
      }
      return acum / (1000 * 60 * 60);
   }

   bus.send("ui-choice-field:create", {
      "div" : "task-view-choice",
      "parentDiv" : divButtonsId,
      "values" : [ {
         "text" : "estimación individual",
         "value" : INDIVIDUAL
      }, {
         "text" : "puesta en común",
         "value" : COMMON
      }, {
         "text" : "progreso del proyecto",
         "value" : PROGRESS
      } ],
      "changeEventName" : "taskViewChanged"
   });
   bus.listen("taskViewChanged", function(e, value) {
      currentView = views[value];
      assert(currentView, "no view selected");
      list.refresh(poker.tasks);
   });

   var list = editableList.create(pokerDetailId);
   list.entryClassName("task-entry");

   list.add(function(text) {
      bus.send("add-task-to-poker", [ poker.name, {
         "name" : text,
         "estimations" : {},
         "wiki" : null,
         "creationTime" : new Date().getTime(),
         "commonEstimation" : null
      } ]);
   });

   list.remove(function(task) {
      bus.send("remove-task", [ task.id ]);
   });

   list.select(function(d) {
      bus.send("show-task-wiki", [ d ]);
   });

   list.renderer(function(d) {
      return d.name;
   });

   list.postProcess(function(selection) {
      currentView(selection);

      selection.attr("title", function(t) {
         var ret = "";
         if (t.keywords) {
            for (var i = 0; i < t.keywords.length; i++) {
               ret += t.keywords[i] + ",";

            }
            ret = ret.substring(0, ret.length - 1);
         }
         return ret + " | creationDate: " + new Date(t.creationTime);
      });

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
         bus.send("ui-show", pokerDetailId);
         currentView = null;
      } else {
         bus.send("ui-hide", pokerDetailId);
      }
   });

   bus.listen("selected-poker", function(e, poker) {
      wsbus.send("get-poker", poker.name);
   });

   bus.listen("updated-poker", function(e, newPoker) {
      poker = newPoker;
      bus.send("ui-set-content", {
         "div" : "poker-detail-title",
         "html" : "<h1>Tareas de " + poker.name + "</h1>"
      });
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
      bus.send("progress-project-estimated-field-value-fill", (100 * getTotalEstimation(poker)) / poker.totalCredits);
      bus.send("progress-project-real-field-value-fill", (100 * getTotalReported(poker)) / poker.totalCredits);
      bus.send("txt-project-totalCredits-field-value-fill", poker.totalCredits);
   });
   function getTotalEstimation(poker) {
      var total = 0;
      for (var i = 0; i < poker.tasks.length; i++) {
         var task = poker.tasks[i];
         if (task.commonEstimation) {
            total += task.commonEstimation;
         }
      }

      return total;
   }
   function getTotalReported(poker) {
      var total = 0;
      for (var i = 0; i < poker.tasks.length; i++) {
         var task = poker.tasks[i];
         total += getTotalTime(task);
      }

      return total;
   }
   bus.listen("updated-task", function(e, newTask) {
      for (var i = 0; i < poker.tasks.length; i++) {
         if (poker.tasks[i].id == newTask.id) {
            poker.tasks[i] = newTask;
            break;
         }
      }
      list.refresh(poker.tasks);
   });
   bus.listen("set-user", function(e, newUserName) {
      userName = newUserName;
   });

});