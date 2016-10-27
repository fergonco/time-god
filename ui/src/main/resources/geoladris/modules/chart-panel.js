define([ "message-bus", "chart", "task-utils" ], function(bus, Chart, taskUtils) {

   var chartId = "chart";

   bus.send("ui-element:create", {
      "type" : "canvas",
      "div" : chartId,
      "parentDiv" : null
   });
   bus.send("ui-hide", chartId);

   bus.listen("ui-update-poker-list", function(e, pokerList) {
      values = [];
      for (var i = 0; i < pokerList.length; i++) {
         var poker = pokerList[i];
         for (var j = 0; j < poker.tasks.length; j++) {
            var task = poker.tasks[j];
            var sample = (taskUtils.getConsumedTime(task) - task.commonEstimation) / task.commonEstimation;
            if (!isNaN(sample) && sample != Infinity) {
               values.push(sample);
            }
         }
      }

      values.sort(function(a, b) {
         return a - b;
      });
      console.log(values);
      var numBins = 40;
      var min = values[0];
      var max = values[values.length - 1];
      var step = (max - min) / (numBins * 1.0);
      var bins = new Array(numBins);
      var binLabels = new Array(numBins);
      for (var i = 0; i < numBins; i++) {
         bins[i] = 0;
      }
      currentBinMax = min + step;
      currentBin = 0;
      for (var i = 0; i < values.length; i++) {
         if (values[i] < currentBinMax) {
            bins[currentBin]++;
         } else {
            binLabels[currentBin] = "< " + currentBinMax;
            bins[currentBin + 1]++;
            currentBinMax += step;
            currentBin++;
         }
      }

      console.log(bins);
      console.log(binLabels);

      var myChart = new Chart(document.getElementById(chartId), {
         type : 'bar',
         data : {
            labels : binLabels,
            datasets : [ {
               data : bins
            } ]
         },
         options : {
            scales : {
               yAxes : [ {
                  ticks : {
                     beginAtZero : true
                  }
               } ]
            }
         }
      });
   });

   bus.listen("show-window", function(e, window) {
      if (window == "chart") {
         bus.send("ui-show", chartId);
      } else {
         bus.send("ui-hide", chartId);
      }
   });

});