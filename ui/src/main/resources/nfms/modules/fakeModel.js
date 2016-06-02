define([ "message-bus" ], function(bus) {

   var developers = [];
   var pokers = [];

   bus.listen("modules-loaded", function() {
      sendList();
   });

   bus.listen("add-developer", function(e, d) {
      developers.push(d);
      sendDevelopers();
   });

   bus.listen("remove-developer", function(e, name) {
      developers.splice(findDeveloperIndex(name), 1);
      sendDevelopers();
   });

   bus.listen("add-poker", function(e, p) {
      pokers.push(p);
      sendList();
   });

   bus.listen("remove-poker", function(e, name) {
      pokers.splice(findPokerIndex(name), 1);
      sendList();
   });

   function findDeveloperIndex(name) {
      return findIndex(developers, name);
   }

   function findIndex(array, name) {
      for (var i = 0; i < array.length; i++) {
         if (array[i].name == name) {
            return i;
         }
      }

      assert(false, "should never reach this point. search term: " + name);
   }

   function findPokerIndex(name) {
      return findIndex(pokers, name);
   }

   function findTaskIndex(poker, name) {
      return findIndex(poker.tasks, name);
   }

   bus.listen("add-task-to-poker", function(e, pokerName, task) {
      var poker = pokers[findPokerIndex(pokerName)];
      poker.tasks.push(task);
      sendPoker(poker);
   });

   bus.listen("remove-task", function(e, pokerName, taskName) {
      var poker = pokers[findPokerIndex(pokerName)];
      poker.tasks.splice(findTaskIndex(poker, taskName), 1);
      sendPoker(poker);
   });

   bus.listen("change-task-user-credits", function(e, userName, pokerName, taskName, credits) {
      var credits = parseInt(credits);
      if (!isNaN(credits)) {
         var poker = pokers[findPokerIndex(pokerName)];
         var task = poker.tasks[findTaskIndex(poker, taskName)];
         task.estimations[userName] = credits;
         sendPoker(poker);
      }
   });

   bus.listen("change-task-common-credits", function(e, pokerName, taskName, credits) {
      var credits = parseInt(credits);
      if (!isNaN(credits)) {
         var poker = pokers[findPokerIndex(pokerName)];
         var task = poker.tasks[findTaskIndex(poker, taskName)];
         task.commonEstimation = credits;
         sendPoker(poker);
      }
   });

   function sendDevelopers() {
      bus.send("updated-developer-list", [ developers ]);
   }

   function sendList() {
      bus.send("updated-poker-list", [ pokers ]);
   }

   function sendPoker(poker) {
      bus.send("updated-poker", [ poker ]);
   }
});