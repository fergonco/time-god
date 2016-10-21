define([ "message-bus", "websocket-bus", "ui-values", "latinize", "auth-user", "auth" ], function(bus, wsbus, uiValues,
   latinize, authUser) {

   var userName = null;
   var dlgAskPokersId = "dlg-ask-pokers";
   var pokers = [];

   bus.listen("modules-loaded", function() {
      bus.send("show-window", [ "developers" ]);

      bus.send("ui-element:create", {
         "div" : "lblUserName",
         "parentDiv" : null,
         "type" : "div",
         "html" : authUser == null ? "No identificado" : "Hola " + authUser
      });

      bus.listen("set-user", function(e, newUserName) {
         userName = newUserName;
         bus.send("ui-set-content", [ {
            "div" : "lblUserName",
            "html" : "Hola " + newUserName
         } ]);
         bus.send("ui-show", "btn-removeTimeReport");
      });

      bus.send("ui-button:create", {
         "div" : "btn-timeReport",
         "parentDiv" : null,
         "text" : "Informe reportes de tiempo",
         "sendEventName" : "show-time-report"
      });

      bus.send("ui-button:create", {
         "div" : "btn-removeTimeReport",
         "parentDiv" : null,
         "text" : "Eliminar informe de tiempo",
         "sendEventName" : "remove-time-report"
      });
      bus.send("ui-hide", "btn-removeTimeReport");
   });

   bus.listen("ui-update-poker-list", function(e, newPokers) {
      pokers = newPokers;
   });

   function format(minutes) {
      return minutes < 10 ? "0" + minutes : minutes;
   }

   function getDeveloperTimes(developerAccum) {
      var ret = "\n";
      var total = 0;
      for ( var developerName in developerAccum) {
         ret += "* Acumulado " + developerName + ": " + Math.round(developerAccum[developerName] / 3600000) + "h\n";
         total += developerAccum[developerName];
      }
      ret += "* Acumulado total: " + Math.round(total / 3600000) + "\n\n\n";
      return ret;
   }

   bus.listen("dlg-ask-pokers-cancel", function(e, message) {
      bus.send("ui-dialog:close", dlgAskPokersId);
   });

   bus.listen("show-time-report", function(e, message) {
      bus.send("ui-dialog:create", {
         div : dlgAskPokersId,
         parentDiv : null,
         modal : true
      });
      bus.send("ui-element:create", {
         "parentDiv" : dlgAskPokersId,
         "type" : "div",
         "html" : "Selecciona los proyectos que quieres en el informe"
      });
      for (var i = 0; i < pokers.length; i++) {
         var poker = pokers[i];
         var chkPokerId = "chkPoker" + latinize.toIdNoPoints(poker.name);
         bus.send("ui-input-field:create", {
            "div" : chkPokerId,
            "parentDiv" : dlgAskPokersId,
            "type" : "checkbox",
            "text" : poker.name
         });
         uiValues.set(chkPokerId, false);
      }

      bus.send("ui-button:create", {
         "div" : "btnReportPokers",
         "parentDiv" : dlgAskPokersId,
         "text" : "Generar informe",
         "sendEventName" : "show-time-report-for-pokers"
      });
      bus.send("ui-button:create", {
         "div" : "btnCancelReport",
         "parentDiv" : dlgAskPokersId,
         "text" : "Cancelar",
         "sendEventName" : "dlg-ask-pokers-cancel"
      });
   });

   bus.listen("remove-time-report", function(e, message) {
      bus.send("jsdialogs.question", [ {
         "message" : "Introduce el código a eliminar",
         "okAction" : function(value) {
            if (value.trim().length > 0) {
               wsbus.send("remove-time-report", {
                  "id" : value.trim(),
                  "developerName" : userName
               });
            }
         }
      } ]);
   });

   bus.listen("show-time-report-for-pokers", function(e) {
      var selectedPokers = [];
      for (var i = 0; i < pokers.length; i++) {
         var poker = pokers[i];
         if (uiValues.get("chkPoker" + latinize.toIdNoPoints(poker.name))) {
            selectedPokers.push(poker);
         }
      }
      bus.send("ui-dialog:close", dlgAskPokersId);

      var timeSegments = [];
      for (var i = 0; i < selectedPokers.length; i++) {
         var poker = selectedPokers[i];
         for (var j = 0; j < poker.tasks.length; j++) {
            var task = poker.tasks[j];
            for (var k = 0; k < task.timeSegments.length; k++) {
               var timeSegment = task.timeSegments[k];
               timeSegments.push({
                  "id" : timeSegment.id,
                  "start" : timeSegment.start,
                  "end" : timeSegment.end,
                  "task" : task,
                  "poker" : poker,
                  "developer" : timeSegment.developer
               });
            }
         }
      }
      timeSegments.sort(function(a, b) {
         return a.start - b.start;
      });

      var wiki = "";
      var lastDay = null;
      var developerAccum = {};
      for (var i = 0; i < timeSegments.length; i++) {
         var start = new Date(timeSegments[i].start);
         var end = new Date(timeSegments[i].end);

         if (lastDay == null || start.getDate() != lastDay.getDate() || start.getMonth() != lastDay.getMonth()
            || start.getFullYear() != lastDay.getFullYear()) {
            wiki += getDeveloperTimes(developerAccum);

            wiki += start.getDate() + "/" + (start.getMonth() + 1) + "/" + start.getFullYear() + "\n";
            wiki += "-".repeat(30) + "\n\n";

            lastDay = start;
         }

         // accumulate on developers
         var developerName = timeSegments[i].developer.name;
         if (!developerAccum.hasOwnProperty(developerName)) {
            developerAccum[developerName] = 0;
         }
         developerAccum[developerName] += timeSegments[i].end - timeSegments[i].start;

         // time segments log
         wiki += format(start.getHours()) + "." + format(start.getMinutes());
         wiki += " - ";
         wiki += format(end.getHours()) + "." + format(end.getMinutes());
         wiki += "\t" + developerName;
         wiki += "\t" + timeSegments[i].poker.name;
         wiki += "\t" + timeSegments[i].task.name;
         wiki += "(" + timeSegments[i].id + ")";
         wiki += "\n";
      }
      wiki += getDeveloperTimes(developerAccum);

      var w = window.open();
      w.document.open();
      w.document.write("<pre>" + wiki + "</pre>");
      w.document.close();

   });

});