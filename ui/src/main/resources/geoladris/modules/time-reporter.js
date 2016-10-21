define([ "message-bus", "websocket-bus", "ui-values", "d3" ], function(bus, wsbus, uiValues, d3) {

   var weekdayNames = [ "Domingo LOL", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado LOL" ];

   var txt = null;
   var currentTask = null;
   var developerName = null;

   var currentDate = null;
   var currentTime = null;

   bus.listen("set-user", function(e, userName) {
      developerName = userName;
   });

   function dayInc(sign) {
      currentDate = new Date(currentDate.getTime() + sign * 24 * 60 * 60 * 1000);
      var day = currentDate.getDate() + "." + (currentDate.getMonth() + 1) + "." + currentDate.getFullYear();
      uiValues.set("txtDay", day);
      currentTime = parseTime();
      refreshUserMessage();
   }
   function refreshUserMessage() {
      var userMessage = "";
      if (currentTime != null) {
         var msDifference = currentTime.end - currentTime.start;
         userMessage = (msDifference / (1000 * 60 * 60)) + "h";
      } else {
         userMessage = "Hora no válida. Debe ser: hh.mm-hh.mm";
      }
      userMessage = weekdayNames[currentDate.getDay()] + "," + userMessage;

      bus.send("ui-set-content", {
         "div" : "msgTimeReporter",
         "html" : userMessage
      });
   }
   bus.listen("time-report-previousDay", function(e, value) {
      dayInc(-1);
   });

   bus.listen("time-report-nextDay", function(e, value) {
      dayInc(1);
   });
   bus.listen("time-report-change", function(e, value) {
      currentTime = parseTime();
      refreshUserMessage();
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
         "text" : "Horario: ",
         "changeEventName" : "time-report-change"
      });

      var divDayId = "divDay";
      bus.send("ui-element:create", {
         "div" : divDayId,
         "parentDiv" : reporterId,
         "type" : "div"
      });

      bus.send("ui-input-field:create", {
         "div" : "txtDay",
         "parentDiv" : divDayId,
         "text" : "Fecha: "
      });
      bus.send("ui-attr", {
         "div" : "txtDay",
         "attribute" : "readonly",
         "value" : true
      });
      bus.send("ui-button:create", {
         "parentDiv" : divDayId,
         "text" : "<",
         "sendEventName" : "time-report-previousDay"
      });
      bus.send("ui-button:create", {
         "parentDiv" : divDayId,
         "text" : ">",
         "sendEventName" : "time-report-nextDay"
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

      currentDate = new Date();
      dayInc(0);

      var now = new Date();
      uiValues.set("txtTime", now.getHours() + "." + twoDigits(now.getMinutes()) + "-" + (now.getHours() + 1) + "."
         + twoDigits(now.getMinutes()));
      bus.send("time-report-change");
   });
   
   function twoDigits(value) {
      if (value < 10) {
         return "0" + value;
      } else {
         return value;
      }
   }

   bus.listen("btnReport-click", function() {
      try {
         var time = parseTime();
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
                        "keywords" : keywords,
                        "developerName" : developerName
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
   function parseTime() {
      var text = uiValues.get("txtTime");
      var timeRegExp = /([0-9]{1,2})[.|:]([0-9]{2})-([0-9]{1,2})[.|:]([0-9]{2})/g;
      var match = timeRegExp.exec(text);
      if (match != null && match.length == 5) {
         var start = getTime(match[1], match[2]);
         var end = getTime(match[3], match[4]);
         return {
            "start" : start,
            "end" : end
         }
      } else {
         return null;
      }
   }

   function getTime(hours, minutes) {
      var date = new Date(currentDate.getTime());
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