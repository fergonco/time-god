define([ "message-bus" ], function(bus) {

   var pokers = [ {
      name : "foo",
      tasks : []
   } ];

   bus.listen("modules-loaded", function() {
      sendList();
   });

   bus.listen("add-poker", function(e, p) {
      pokers.push(p);
      sendList();
   });

   bus.listen("remove-poker", function(e, name) {
      pokers.splice(findPokerIndex(name), 1);
      sendList();
   });

   function findPokerIndex(name) {
      for (var i = 0; i < pokers.length; i++) {
         if (pokers[i].name == name) {
            return i;
         }
      }

      assert(false, "should never reach this point. Poker name: " + name);
   }

   bus.listen("add-task-to-poker", function(e, pokerName, task) {
      var poker = pokers[findPokerIndex(pokerName)];
      poker.tasks.push(task);
      sendPoker(poker);
   });

   bus.listen("change-poker-total-credits", function(e, pokerName, totalCredits) {
      var credits = parseInt(totalCredits);
      if (!isNaN(credits)) {
         var poker = pokers[findPokerIndex(pokerName)];
         poker.totalCredits = credits;
         sendPoker(poker);
      }
   });

   function sendList() {
      bus.send("updated-poker-list", [ pokers ]);
   }

   function sendPoker(poker) {
      bus.send("updated-poker", [ poker ]);
   }
});