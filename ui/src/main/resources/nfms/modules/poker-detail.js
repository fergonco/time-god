define([ "d3", "message-bus", "websocket-bus", "editableList", "markdown" ], function(d3, bus, wsbus, editableList) {

   var userName = null;
   var poker = null;
   var pokerName = null;
   var currentView = null;

   var pokerDetailId = "poker-detail";

   bus.send("ui-element:create", {
      "type" : "div",
      "div" : pokerDetailId,
      "parentDiv" : null
   });
   bus.send("ui-hide", pokerDetailId);

   bus.send("ui-element:create", {
      "type" : "h1",
      "div" : "poker-detail-title",
      "parentDiv" : pokerDetailId
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

   bus.send("ui-button:create", {
      "div" : "poker-detail-registerEvent",
      "parentDiv" : divButtonsId,
      "text" : "Registrar evento",
      "sendEventName" : "register-event"
   });

   bus.listen("register-event", function(e, message) {
      var eventTaxonomyListener = function(e, type, keywords) {
         if (type == "event") {
            bus.stopListen("taxonomy-processed", eventTaxonomyListener);
            wsbus.send("add-poker-event", {
               "pokerName" : poker.name,
               "timestamp" : new Date().getTime(),
               "keywords" : keywords
            });
         }
      };
      bus.listen("taxonomy-processed", eventTaxonomyListener);
      bus.send("show-taxonomy", [ d3.select("#poker-detail-registerEvent").node(), "event" ]);
   });

   bus.send("ui-button:create", {
      "div" : "poker-detail-htmlReport",
      "parentDiv" : divButtonsId,
      "text" : "Informe HTML",
      "sendEventName" : "show-report",
      "sendEventMessage" : "html"
   });

   bus.send("ui-button:create", {
      "div" : "poker-detail-markupReport",
      "parentDiv" : divButtonsId,
      "text" : "Informe Markup",
      "sendEventName" : "show-report",
      "sendEventMessage" : "markup"
   });

   bus.listen("show-report", function(e, message) {
      var wiki = "# " + poker.name + "\n";
      wiki += "Consumido: " + Math.round((100 * getTotalReported(poker)) / poker.totalCredits) + "% de "
         + poker.totalCredits + "\n";

      for (var i = 0; i < poker.tasks.length; i++) {
         var task = poker.tasks[i];
         wiki += "## " + task.name + "\n";
         wiki += "Consumido: " + Math.round((100 * getTotalTime(task)) / task.commonEstimation) + "% de "
            + task.commonEstimation + "\n";
         if (task.wiki) {
            var taskWiki = task.wiki;
            for (var j = 5; j > 0; j--) {
               var pattern = "#".repeat(j);
               taskWiki = taskWiki.replace(new RegExp(pattern, "g"), "##" + pattern);
            }
            wiki += taskWiki + "\n";
         }
      }

      var w = window.open();
      w.document.open();
      if (message == "html") {
         w.document.write("<html><head><link rel=\"stylesheet\" href=\"modules/poker-detail-report.css\"></head><body>"
            + markdown.toHTML(wiki) + "</body></html>");
      } else {
         w.document.write("<pre>" + wiki + "</pre>");
      }
      w.document.close();
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

   bus.send("ui-element:create", {
      "div" : "poker-detail-events",
      "parentDiv" : pokerDetailId,
      "type" : "pre"
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
      "parentDiv" : pokerDetailId,
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
      bus.send("show-task-wiki", [ d.id ]);
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

   bus.listen("selected-poker", function(e, newPokerName) {
      pokerName = newPokerName;
      bus.send("get-poker", newPokerName);
   });

   bus.listen("ui-update-poker",
      function(e, newPoker) {
         if (pokerName != null && pokerName == newPoker.name) {
            poker = newPoker;
            // poker name
            bus.send("ui-set-content", {
               "div" : "poker-detail-title",
               "html" : poker.name
            });

            // task list
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

            // progress bars
            bus.send("progress-project-estimated-field-value-fill", (100 * getTotalEstimation(poker))
               / poker.totalCredits);
            bus.send("progress-project-real-field-value-fill", (100 * getTotalReported(poker)) / poker.totalCredits);
            bus.send("txt-project-totalCredits-field-value-fill", poker.totalCredits);

            // event list
            var eventReport = "Eventos:\n";
            for (var i = 0; i < poker.events.length; i++) {
               var event = poker.events[i];
               eventReport += new Date(event.timestamp) + "\t" + event.keywords + "\n";
            }
            bus.send("ui-set-content", {
               "div" : "poker-detail-events",
               "html" : eventReport
            });

         }
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

   bus.listen("set-user", function(e, newUserName) {
      userName = newUserName;
   });

});