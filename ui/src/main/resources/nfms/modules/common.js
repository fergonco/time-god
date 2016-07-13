define([ "message-bus" ], function(bus) {

   bus.listen("modules-loaded", function() {
      bus.send("show-window", [ "developers" ]);

      bus.send("ui-element:create", [ {
         "div" : "lblUserName",
         "parentDiv" : null,
         "type" : "div",
         "html" : "No identificado"
      } ]);

      bus.listen("set-user", function(e, userName) {
         bus.send("ui-set-content", [ {
            "div" : "lblUserName",
            "html" : "Hola " + userName
         } ]);
      });

      bus.send("ui-button:create", {
         "div" : "btn-timeReport",
         "parentDiv" : null,
         "text" : "Informe reportes de tiempo",
         "sendEventName" : "show-time-report"
      });

   });

   bus.listen("ui-update-poker-list", function(e, newPokers) {
      pokers = newPokers;
   });

   function format(minutes) {
      return minutes < 10 ? "0" + minutes : minutes;
   }

   bus.listen("show-time-report", function(e, message) {
      var timeSegments = [];
      for (var i = 0; i < pokers.length; i++) {
         var poker = pokers[i];
         for (var j = 0; j < poker.tasks.length; j++) {
            var task = poker.tasks[j];
            for (var k = 0; k < task.timeSegments.length; k++) {
               var timeSegment = task.timeSegments[k];
               timeSegments.push({
                  "start" : timeSegment.start,
                  "end" : timeSegment.end,
                  "task" : task,
                  "poker" : poker
               });
            }
         }
      }
      timeSegments.sort(function(a, b) {
         return a.start - b.start;
      });

      var wiki = "";
      var lastDay = null;
      for (var i = 0; i < timeSegments.length; i++) {
         var start = new Date(timeSegments[i].start);
         var end = new Date(timeSegments[i].end);
         if (lastDay == null || start.getDate() != lastDay.getDate() || start.getMonth() != lastDay.getMonth()
            || start.getFullYear() != lastDay.getFullYear()) {
            if (wiki.length > 0) {
               wiki += "\n\n";
            }
            wiki += start.getDate() + "/" + start.getMonth() + "/" + start.getFullYear() + "\n";
            wiki += "-".repeat(30) + "\n";
            
            lastDay = start;
         }

         wiki += format(start.getHours()) + "." + format(start.getMinutes());
         wiki += " - ";
         wiki += format(end.getHours()) + "." + format(end.getMinutes());
         wiki += "\t" + timeSegments[i].poker.name;
         wiki += "\t" + timeSegments[i].task.name;
         wiki += "\n";
      }

      var w = window.open();
      w.document.open();
      w.document.write("<pre>" + wiki + "</pre>");
      w.document.close();

   });

});