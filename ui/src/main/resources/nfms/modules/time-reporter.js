define([ "message-bus", "websocket-bus", "ui-values", "d3" ], function(bus, wsbus, uiValues, d3) {

   var txt = null;
   var currentTask = null;

   bus.listen("time-report-change", function(e, value) {
      var userMessage = "";
      try {
         var time = parse(value);
         var msDifference = time.end - time.start;
         userMessage = (msDifference / (1000 * 60 * 60)) + "h";
      } catch (e) {
         userMessage = e;
      }
      bus.send("ui-set-content", {
         "div" : "msgTimeReporter",
         "html" : userMessage
      });
   });

   bus.listen("report-time", function(e, task) {
      currentTask = task;
      var reporterId = "dedication-reporter";
      var reporter = d3.select("body").append("div")//
      .attr("id", "time-overlay")//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("id", reporterId);

      bus.send("ui-input-field:create", {
         "div" : "txtTime",
         "parentDiv" : reporterId,
         "changeEventName" : "time-report-change"
      });

      bus.send("ui-button:create", {
         "div" : "btnReport",
         "parentDiv" : reporterId,
         "text" : "Reportar",
         "sendEventName" : "btnReport-click"
      });

      bus.send("ui-element:create", {
         "div" : "msgTimeReporter",
         "parentDiv" : reporterId,
         "type" : "span"
      });
   });

   bus.listen("btnReport-click", function() {
      try {
         var time = parse(uiValues.get("txtTime"));
         var taxonomyProcessedListener = function(e, type, keywords) {
            if (type == "time") {
               bus.stopListen("taxonomy-processed", taxonomyProcessedListener);
               msg = "Resumen:<ul>" //
                  + "<li>Tarea: " + currentTask.name//
                  + "</li><li>Inicio: " + new Date(time.start)//
                  + "</li><li>Fin: " + new Date(time.end)//
                  + "</li><li>Keywords: " + keywords + "</li></ul>";
               var dialogOptions = {
                  "message" : msg,
                  "okAction" : function() {
                     wsbus.send("report-task-time", {
                        "taskId" : currentTask.id,
                        "timeStart" : time.start,
                        "timeEnd" : time.end,
                        "keywords" : keywords
                     });
                  },
                  "closeAction" : function() {
                     d3.select("#time-overlay").remove();
                  }
               };
               bus.send("jsdialogs.confirm", [ dialogOptions ]);
            }
         };
         bus.listen("taxonomy-processed", taxonomyProcessedListener);
         bus.send("show-taxonomy", [ d3.select("#btnReport").node(), "time" ]);
      } catch (e) {
         alert(e);
      }
   });
   function parse(text) {
      var timeRegExp = /([0-9]{1,2})[.|:]([0-9]{2})-([0-9]{1,2})[.|:]([0-9]{2})/g;
      var match = timeRegExp.exec(text);
      if (match != null && match.length == 5) {
         var start = getTime(match[1], match[2]);
         var end = getTime(match[3], match[4]);
         var now = new Date().getTime();
         if (start > now || end > now) {
            var dayMillis = 1000 * 60 * 60 * 24;
            start -= dayMillis;
            end -= dayMillis;
         }
         return {
            "start" : start,
            "end" : end
         }
      } else {
         throw "Error de formato. Debe ser hh.mm-hh.mm";
      }
   }

   function getTime(hours, minutes) {
      var date = new Date();
      date.setHours(toInt(hours));
      date.setMinutes(toInt(minutes));
      return date.getTime();
   }

   function toInt(txt) {
      var ret = parseInt(txt);
      if (isNaN(ret)) {
         throw "Se esperaba un número: " + txt;
      } else {
         return ret;
      }
   }

});