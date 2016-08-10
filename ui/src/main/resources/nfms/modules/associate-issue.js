define([ "message-bus", "websocket-bus", "ui-values" ], function(bus, wsbus, uiValues) {

   var userName = null;
   var dialogDOMId = "dlgAssociateIssues";

   bus.listen("associate-issue", function(e, taskAndPoker) {
      wsbus.send("proxy", {
         "url" : taskAndPoker.poker.apiRepository + "issues",
         "event-name" : "associate-issue-show-issue-list",
         "context" : {
            "taskId" : taskAndPoker.task.id
         }
      });

   });

   bus.listen("associate-issue-show-issue-list", function(e, message) {
      var data = message.response;
      var taskId = message.context.taskId;
      bus.send("ui-element:create", {
         "div" : dialogDOMId,
         "parentDiv" : null,
         "type" : "div",
         "html" : "Selecciona las issues a asociar"
      });
      for (var i = 0; i < data.length; i++) {
         var assignee = data[i].assignee ? " (" + data[i].assignee.login + ")" : "";
         bus.send("ui-input-field:create", {
            "div" : "chkAssociateIssue-" + data[i].number,
            "parentDiv" : dialogDOMId,
            "type" : "checkbox",
            "text" : "#" + data[i].number + " " + data[i].title + assignee
         });
      }
      var dialogOptions = {
         "okAction" : function() {
            var issueNumbers = [];
            for (var i = 0; i < data.length; i++) {
               if (uiValues.get("chkAssociateIssue-" + data[i].number)) {
                  issueNumbers.push(data[i].number);
               }
            }
            wsbus.send("associate-task-issue", {
               "taskId" : taskId,
               "issueNumbers" : issueNumbers,
               "developerName" : userName
            });
         },
         "div" : dialogDOMId
      };
      bus.send("jsdialogs.confirm", [ dialogOptions ]);
   });

   bus.listen("set-user", function(e, newUserName) {
      userName = newUserName;
   });

});