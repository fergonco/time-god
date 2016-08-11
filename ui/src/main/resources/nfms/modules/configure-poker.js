define([ "message-bus", "websocket-bus", "ui-values", "editableList" ], function(bus, wsbus, uiValues, editableList) {

   var userName = null;

   bus.listen("configure-poker", function(e, poker) {
      var issueRepositories = poker.issueRepositories ? poker.issueRepositories.slice(0) : [];
      var pokerConfigurationId = "poker-configuration";
      bus.send("ui-element:create", {
         "div" : pokerConfigurationId,
         "parentDiv" : null,
         "type" : "div"
      });
      
      bus.send("ui-element:create", {
         "type" : "div",
         "div": "repo-list-span",
         "parentDiv" : pokerConfigurationId,
      });
      bus.send("ui-set-content", {
         "div": "repo-list-span",
         "html" : "Lista de repositorios",
      });
      
      var repoListContainerId = "repo-list-container";
      bus.send("ui-element:create", {
         "type" : "div",
         "div" : repoListContainerId,
         "parentDiv" : pokerConfigurationId,
      });
      
      var githubRepositoriesId = "github-repositories";
      bus.send("ui-element:create", {
         "type" : "div",
         "div" : githubRepositoriesId,
         "parentDiv" : repoListContainerId,
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