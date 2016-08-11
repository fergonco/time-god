define([ "message-bus", "websocket-bus", "ui-values", "editableList" ], function(bus, wsbus, uiValues, editableList) {

   var userName = null;

   bus.listen("configure-poker", function(e, poker) {
      var issueRepositories = poker.issueRepositories ? poker.issueRepositories : [];
      var pokerConfigurationId = "poker-configuration";
      bus.send("ui-element:create", {
         "div" : pokerConfigurationId,
         "parentDiv" : null,
         "type" : "div"
      });
      var githubRepositoriesId = "github-repositories";
      bus.send("ui-element:create", {
         "type" : "div",
         "div" : githubRepositoriesId,
         "parentDiv" : pokerConfigurationId,
      });

      var list = editableList.create(githubRepositoriesId);
      list.entryClassName("github-repository-entry");

      list.add(function(text) {
         issueRepositories.push("github/" + text);
         list.refresh(issueRepositories);
      });

      list.remove(function(repo) {
         for (var i = 0; i < issueRepositories.length; i++) {
            if (issueRepositories[i] === repo) {
               issueRepositories.splice(i, 1);
               break;
            }
         }
         list.refresh(issueRepositories);
      });

      list.renderer(function(repo) {
         return repo;
      });
      list.refresh(issueRepositories);

      bus.send("ui-input-field:create", {
         "div" : pokerConfigurationId + "-wiki-root",
         "parentDiv" : pokerConfigurationId,
         "text" : "Repositorio wiki (e.g.: https://github.com/fergonco/time-god/)"
      });
      uiValues.set(pokerConfigurationId + "-wiki-root", poker.wikiRepository);

      var dialogOptions = {
         "okAction" : function() {
            wsbus.send("change-poker-repository-configuration", {
               "pokerName" : poker.name,
               "issueRepositories" : issueRepositories,
               "wikiRepository" : uiValues.get(pokerConfigurationId + "-wiki-root"),
               "developerName" : userName
            });
         },
         "div" : pokerConfigurationId
      };
      bus.send("jsdialogs.confirm", [ dialogOptions ]);

   });
   bus.listen("set-user", function(e, newUserName) {
      userName = newUserName;
   });

});