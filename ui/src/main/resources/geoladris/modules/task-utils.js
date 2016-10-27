define([  ], function() {

   return {
      getConsumedTime : function(task) {
         var acum = 0;
         if (task.timeSegments) {
            for (var i = 0; i < task.timeSegments.length; i++) {
               var timeSegment = task.timeSegments[i];
               acum += timeSegment.end - timeSegment.start;
            }
         }
         return acum / (1000 * 60 * 60);
      }
   }
});