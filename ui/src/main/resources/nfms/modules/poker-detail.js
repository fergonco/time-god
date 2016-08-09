define([ "d3", "message-bus", "websocket-bus", "editableList", "latinize", "markdown" ], function(d3, bus, wsbus,
   editableList, latinize) {

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

   bus.send("ui-button:create", {
      "div" : "poker-detail-configurePoker",
      "parentDiv" : divButtonsId,
      "text" : "Configuración",
      "sendEventName" : "configure-poker"
   });

   bus.listen("register-event", function(e, message) {
      var eventTaxonomyListener = function(e, type, keywords) {
         if (type == "event") {
            bus.stopListen("taxonomy-processed", eventTaxonomyListener);
            wsbus.send("add-poker-event", {
               "developerName" : userName,
               "pokerName" : poker.name,
               "timestamp" : new Date().getTime(),
               "keywords" : keywords
            });
         }
      };
      bus.listen("taxonomy-processed", eventTaxonomyListener);
      bus.send("show-taxonomy", [ d3.select("#poker-detail-registerEvent").node(), "event" ]);
   });

   bus.listen("configure-poker", function() {
      var dialogOptions = {
         "message" : "Introduce el repositorio de issues",
         "okAction" : function(value) {
            wsbus.send("change-poker-issueRepository", {
               "pokerName" : poker.name,
               "issueRepository" : value,
               "developerName" : userName
            });
         },
         "initialValue" : poker.issueRepository
      };
      bus.send("jsdialogs.question", [ dialogOptions ]);
   });

   bus.listen("totalCreditsUpdated", function(e, value) {
      wsbus.send("change-poker-totalCredits", {
         "developerName" : userName,
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
         wsbus.send("change-task-user-credits", {
            "userName" : userName,
            "taskId" : task.id,
            "credits" : this.value,
            "developerName" : userName
         });

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
                  "keywords" : keywords,
                  "developerName" : userName
               });
            }
         };
         bus.listen("taxonomy-processed", taxonomyProcessedListener);

         bus.send("show-taxonomy", [ this, COMMON ]);
         wsbus.send("change-task-common-credits", {
            "taskId" : task.id,
            "credits" : this.value,
            "developerName" : userName
         });

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
      wsbus.send("add-task-to-poker", {
         "pokerName" : poker.name,
         "developerName" : userName,
         "task" : {
            "name" : text,
            "estimations" : {},
            "creationTime" : new Date().getTime(),
            "commonEstimation" : null
         }
      });
   });

   list.remove(function(task) {
      wsbus.send("remove-task", {
         "taskId" : task.id,
         "developerName" : userName
      });
   });

   list.select(function(d) {
   });

   list.renderer(function(d) {
      return d.name;
   });

   list.postProcess(function(selection) {

      selection//
      .append("span")//
      .each(function(d) {
         bus.send("ui-button:create", {
            "element" : this,
            "text" : "Renombrar",
            "sendEventName" : "rename-task",
            "sendEventMessage" : d
         });
      });

      selection//
      .append("span")//
      .each(function(d) {
         bus.send("ui-button:create", {
            "element" : this,
            "text" : "Wiki",
            "sendEventName" : "show-wiki",
            "sendEventMessage" : d.name
         });
      });

      selection//
      .append("span")//
      .each(function(d) {
         bus.send("ui-button:create", {
            "element" : this,
            "text" : "Refrescar",
            "sendEventName" : "get-poker",
            "sendEventMessage" : poker.name
         });
      });
      selection//
      .append("span")//
      .each(function(d) {
         bus.send("ui-button:create", {
            "element" : this,
            "text" : "Crear issue",
            "sendEventName" : "create-issue",
            "sendEventMessage" : d
         });
      });

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

      var issueSelection = selection.append("ul").selectAll(".issue").data(function(task) {
         var issues = task.issues ? task.issues : [];
         var ret = [];
         for (var i = 0; i < issues.length; i++) {
            ret.push({
               "task" : task,
               "issueNumber" : issues[i]
            });
         }

         return ret;
      });
      function getIssueElementId(taskAndIssue) {
         return "task-" + taskAndIssue.task.id + "-issue-" + taskAndIssue.issueNumber
      }
      issueSelection.exit().remove();
      var repo = "https://github.com/fergonco/time-god/";
      issueSelection.enter().append("li")//
      .attr("id", function(taskAndIssue) {
         return getIssueElementId(taskAndIssue);
      })//
      .attr("class", "issue")//
      .html(function(taskAndIssue) {
         return repo + "issues/" + taskAndIssue.issueNumber;
      })//
      .each(function(taskAndIssue) {
         var issueDOMId = getIssueElementId(taskAndIssue);
         bus.send("ajax", {
            "url" : poker.issueRepository + "issues/" + taskAndIssue.issueNumber,
            "cache" : false,
            "success" : function(data) {

               bus.send("ui-set-content", {
                  "div" : issueDOMId,
                  "html" : ""
               });
               console.log("setting class from " + issueDOMId + " to " + data.state);
               bus.send("ui-attr", {
                  "div" : issueDOMId,
                  "attribute" : "class",
                  "value" : "issue issue-" + data.state
               });
               bus.send("ui-attr", {
                  "div" : issueDOMId,
                  "attribute" : "title",
                  "value" : data.body
               });

               bus.send("ui-element:create", {
                  "div" : issueDOMId + "-imgUser",
                  "parentDiv" : issueDOMId,
                  "type" : "img"
               });
               bus.send("ui-attr", {
                  "div" : issueDOMId + "-imgUser",
                  "attribute" : "src",
                  "value" : data.assignee ? data.assignee.avatar_url + "&s=15" : "modules/transparent-pixel.png"
               });
               bus.send("ui-attr", {
                  "div" : issueDOMId + "-imgUser",
                  "attribute" : "title",
                  "value" : data.assignee ? data.assignee.login : "sin asignar"
               });

               bus.send("ui-element:create", {
                  "div" : issueDOMId + "-a",
                  "parentDiv" : issueDOMId,
                  "type" : "a"
               });
               bus.send("ui-attr", {
                  "div" : issueDOMId + "-a",
                  "attribute" : "href",
                  "value" : repo + "issues/" + taskAndIssue.issueNumber
               });
               bus.send("ui-attr", {
                  "div" : issueDOMId + "-a",
                  "attribute" : "target",
                  "value" : "_blank"
               });
               bus.send("ui-set-content", {
                  "div" : issueDOMId + "-a",
                  "html" : "#" + taskAndIssue.issueNumber + " " + data.title
               });

            },
            "errorMsg" : "Could not get information about the issue #" + taskAndIssue.issueNumber
         });
      });

   });

   bus.listen("show-wiki", function(e, taskName) {
      window.open("https://github.com/michogar/fao-workplan/blob/master/" + urlize(pokerName) + "/" + urlize(taskName)
         + ".md", "_blank");
   });

   bus.listen("create-issue", function(e, task) {
      wsbus.send("add-task-issue", {
         "developerName" : userName,
         "taskId" : task.id
      });
   });

   function urlize(input) {
      input = latinize(input);
      input = input.replace(/\s/g, "_");
      return input;
   }

   bus.listen("rename-task", function(e, task) {
      var dialogOptions = {
         "message" : "Introduce el nuevo nombre de la tarea",
         "okAction" : function(value) {
            wsbus.send("change-task-name", {
               "taskId" : task.id,
               "name" : value,
               "developerName" : userName
            });
         },
         "initialValue" : task.name
      };
      bus.send("jsdialogs.question", [ dialogOptions ]);
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
            if (poker.events) {
               for (var i = 0; i < poker.events.length; i++) {
                  var event = poker.events[i];
                  eventReport += new Date(event.timestamp) + "\t" + event.keywords + "\n";
               }
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