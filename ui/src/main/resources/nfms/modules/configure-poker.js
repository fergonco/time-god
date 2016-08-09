define([ "message-bus", "websocket-bus", "ui-values" ], function(bus, wsbus, uiValues) {

   var userName = null;

   bus.listen("configure-poker", function(e, poker) {
      var pokerConfigurationId = "poker-configuration";
      bus.send("ui-element:create", {
         "div" : pokerConfigurationId,
         "parentDiv" : null,
         "type":"div"
      });

      bus.send("ui-input-field:create", {
         "div" : pokerConfigurationId + "-api-root",
         "parentDiv" : pokerConfigurationId,
         "text" : "Repositorio a través de la API (e.g.: https://api.github.com/repos/fergonco/time-god/)"
      });
      uiValues.set(pokerConfigurationId + "-api-root", poker.apiRepository);

      bus.send("ui-input-field:create", {
         "div" : pokerConfigurationId + "-web-root",
         "parentDiv" : pokerConfigurationId,
         "text" : "Repositorio web (e.g.: https://github.com/fergonco/time-god/)"
      });
      uiValues.set(pokerConfigurationId + "-web-root", poker.webRepository);

      var dialogOptions = {
         "okAction" : function() {
            wsbus.send("change-poker-repository-configuration", {
               "pokerName" : poker.name,
               "apiRepository" : uiValues.get(pokerConfigurationId + "-api-root"),
               "webRepository" : uiValues.get(pokerConfigurationId + "-web-root"),
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