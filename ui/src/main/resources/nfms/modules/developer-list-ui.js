define([ "d3", "message-bus", "websocket-bus", "editableList" ], function(d3, bus, wsbus, editableList) {

   var container = d3.select("body").append("div").attr("id", "developer-list");
   container.style("display", "none");

   var spnTitle = container.append("h1").html("devélopers devélopers devélopers");

   var list = editableList.create(container);

   return {
      addDeveloper : list.add,
      removeDeveloper : list.remove,
      selectDeveloper : list.select,
      renderer : list.renderer,
      refresh : list.refresh,
      entryClassName : list.entryClassName,
      show : function() {
         container.style("display", "block");
      },
      hide : function() {
         container.style("display", "none");
      }
   }
});