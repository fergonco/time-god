define([ "message-bus", "websocket-bus" ], function(bus, wsbus) {

   bus.listen("add-developer", function(e, d) {
      wsbus.send("add-developer", d);
   });

   bus.listen("remove-developer", function(e, name) {
      wsbus.send("remove-developer", name);
   });

   bus.listen("add-poker", function(e, p) {
      wsbus.send("add-poker", p);
   });

   bus.listen("remove-poker", function(e, name) {
      wsbus.send("remove-poker", name);
   });

   bus.listen("add-task-to-poker", function(e, pokerName, task) {
      wsbus.send("add-task-to-poker", {
         "pokerName" : pokerName,
         "task" : task
      });
   });

   bus.listen("remove-task-from-poker", function(e, pokerName, taskName) {
      wsbus.send("remove-task-from-poker", {
         "pokerName" : pokerName,
         "taskName" : taskName
      });
   });

   bus.listen("change-task-user-credits", function(e, userName, pokerName, taskName, credits) {
      wsbus.send("change-task-user-credits", {
         "userName" : userName,
         "pokerName" : pokerName,
         "taskName" : taskName,
         "credits" : credits
      });
   });

   bus.listen("change-task-common-credits", function(e, pokerName, taskName, credits) {
      wsbus.send("change-task-common-credits", {
         "pokerName" : pokerName,
         "taskName" : taskName,
         "credits" : credits
      });
   });

});