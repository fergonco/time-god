define([ "message-bus" ], function(bus) {

   bus.listen("create-issue", function(e, taskNameAndRepo) {
      window.open(taskNameAndRepo.issueRepository + "/issues/new?title=" + taskNameAndRepo.taskName, "_blank");
   });
});