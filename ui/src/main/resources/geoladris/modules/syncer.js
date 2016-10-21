define([ "message-bus", "websocket-bus", "auth-user" ], function(bus, wsbus, authUser) {

   var developers = [];
   var pokers = [];

   bus.listen("modules-loaded", function(e, message) {
      if (authUser != null) {
         wsbus.send("get-developers");
         wsbus.send("get-pokers");
      }
   });

   /*
    * server update events
    */

   bus.listen("updated-developer-list", function(e, developerList) {
      developers = developerList;
      fireDeveloperList();
   });

   bus.listen("developer-added", function(e, developer) {
      developers.push(developer);
      fireDeveloperList();
   });
   bus.listen("developer-removed", function(e, developerName) {
      for (var i = 0; i < developers.length; i++) {
         if (developers[i].name == developerName) {
            developers.splice(i, 1);
            break;
         }
      }
      fireDeveloperList();
   });

   bus.listen("updated-poker-list", function(e, pokerList) {
      pokers = pokerList;
      firePokerList();
   });

   bus.listen("poker-added", function(e, poker) {
      pokers.push(poker);
      firePokerList();
   });
   bus.listen("poker-removed", function(e, pokerName) {
      pokers.splice(getPokerIndex(pokerName), 1);
      firePokerList();
   });
   function getPokerIndex(name) {
      for (var i = 0; i < pokers.length; i++) {
         if (pokers[i].name == name) {
            return i;
         }
      }

      return null;
   }
   bus.listen("updated-poker", function(e, poker) {
      pokers[getPokerIndex(poker.name)] = poker;
      firePokerList();
   });
   bus.listen("task-added", function(e, message) {
      var poker = pokers[getPokerIndex(message.pokerName)];
      poker.tasks.push(message.task);
      firePoker(poker);
   });
   bus.listen("task-removed", function(e, message) {
      var poker = pokers[getPokerIndex(message.pokerName)];
      poker.tasks.splice(getTaskIndex(poker.tasks, message.taskId), 1);
      firePoker(poker);
   });
   function getTaskIndex(tasks, id) {
      for (var i = 0; i < tasks.length; i++) {
         if (tasks[i].id == id) {
            return i;
         }
      }
      return null;
   }
   bus.listen("updated-task", function(e, message) {
      var poker = pokers[getPokerIndex(message.pokerName)];
      poker.tasks[getTaskIndex(poker.tasks, message.task.id)] = message.task;
      firePoker(poker);
   });

   /*
    * local getters
    */
   bus.listen("get-poker", function(e, pokerName) {
      firePoker(pokers[getPokerIndex(pokerName)]);
   });
   bus.listen("get-task", function(e, taskId) {
      for (var i = 0; i < pokers.length; i++) {
         var index = getTaskIndex(pokers[i].tasks, taskId);
         if (index != null) {
            fireTask(pokers[i].tasks[index]);
            break;
         }
      }
   });

   /*
    * Local update events
    */
   function fireDeveloperList() {
      bus.send("ui-update-developer-list", [ developers ]);
   }
   function firePokerList() {
      bus.send("ui-update-poker-list", [ pokers ]);
      for (var i = 0; i < pokers.length; i++) {
         firePoker(pokers[i]);
      }
   }
   function firePoker(poker) {
      bus.send("ui-update-poker", poker);
      for (var i = 0; i < poker.tasks.length; i++) {
         fireTask(poker.tasks[i]);
      }
   }
   function fireTask(task) {
      bus.send("ui-update-task", task);
   }

});