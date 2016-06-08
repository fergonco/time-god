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

   bus.listen("remove-task", function(e, taskId) {
      wsbus.send("remove-task", {
         "taskId" : taskId
      });
   });

   bus.listen("change-task-user-credits", function(e, userName, taskId, credits) {
      wsbus.send("change-task-user-credits", {
         "userName" : userName,
         "taskId" : taskId,
         "credits" : credits
      });
   });

   bus.listen("change-task-common-credits", function(e, taskId, credits) {
      wsbus.send("change-task-common-credits", {
         "taskId" : taskId,
         "credits" : credits
      });
   });

});