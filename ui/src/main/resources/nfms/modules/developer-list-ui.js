define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var developerListId = "developer-list";
   
   bus.send("ui-element:create", {
      "type" : "div",
      "div" : developerListId,
      "parentDiv" : null,
      "html" : "<h1>devélopers devélopers devélopers</h1>"
   });

   bus.send("ui-hide", developerListId);
   var list = editableList.create(developerListId);

   return {
      addDeveloper : list.add,
      removeDeveloper : list.remove,
      selectDeveloper : list.select,
      renderer : list.renderer,
      refresh : list.refresh,
      entryClassName : list.entryClassName,
      show : function() {
         bus.send("ui-show", developerListId);
      },
      hide : function() {
         bus.send("ui-hide", developerListId);
      }
   }
});