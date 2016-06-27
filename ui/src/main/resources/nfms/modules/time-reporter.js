define([ "message-bus", "websocket-bus", "d3" ], function(bus, wsbus, d3) {

   var task = null;
   var keywords = [];
   var choiceQueue = [];

   bus.listen("report-time", function(e, task) {
      var reporter = d3.select("body").append("div")//
      .attr("id", "time-overlay")//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("id", "dedication-reporter");

      var span = null;

      var txt = reporter.append("input")//
      .attr("type", "text")//
      .on("keyup", function() {
         try {
            var time = parse(txt.property("value"));
            var msDifference = time.end - time.start;
            span.html((msDifference / (1000 * 60 * 60)) + "h");
         } catch (e) {
            span.html(e);
         }
      });

      reporter.append("span")//
      .attr("class", "span-button")//
      .html("reportar")//
      .on("click", function() {
         try {
            var time = parse(txt.property("value"));
            var taxonomyProcessedListener = function(e, type, keywords) {
               if (type == "time") {
                  bus.stopListen("taxonomy-processed", taxonomyProcessedListener);
                  String
                  msg = "Resumen:<ul>" //
                     + "<li>Tarea: " + task.name//
                     + "</li><li>Inicio: " + new Date(time.start)//
                     + "</li><li>Fin: " + new Date(time.end)//
                     + "</li><li>Keywords: " + keywords + "</li></ul>";
                  var dialogOptions = {
                     "message" : msg,
                     "okAction" : function() {
                        wsbus.send("report-task-time", {
                           "taskId" : task.id,
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
            bus.send("show-taxonomy", [ this, "time" ]);
         } catch (e) {
            alert(e);
         }
      });

      span = reporter.append("span");
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