define([ "message-bus", "websocket-bus", "d3", "markdown" ], function(bus, wsbus, d3) {

   var wikiOverlayId = "wiki-overlay";

   var currentTaskId;
   var cancelText;
   var txt;
   var render;
   var btnOk;
   var btnCancel;
   var btnEdit;
   var btnClose;

   function setEditMode(flag) {
      d3.select("#txtWiki").style("display", flag ? "block" : "none");
      d3.select("#wiki-render").style("display", flag ? "none" : "block");

      d3.select("#btnOk").style("display", flag ? "inline" : "none");
      d3.select("#btnCancel").style("display", flag ? "inline" : "none");
      d3.select("#btnEdit").style("display", flag ? "none" : "inline");
      d3.select("#btnClose").style("display", flag ? "none" : "inline");
   }

   function updateWikiCallback(e, task) {
      if (task.id == currentTaskId) {
         txt.property("value", task.wiki);
         render.html(markdown.toHTML(task.wiki != null ? task.wiki : ""));
      }
   }

   bus.listen("show-task-wiki", function(e, task) {
      currentTaskId = task.id;
      d3.select("#" + wikiOverlayId).remove();
      var container = d3.select("body").append("div")//
      .attr("id", wikiOverlayId)//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("id", "wiki-content")//
      .attr("class", "wiki-content")//
      .style("display", "inline-block");

      render = container.append("div")//
      .attr("id", "wiki-render")//
      .attr("class", "content");

      txt = container.append("textarea")//
      .attr("id", "txtWiki")//
      .on("input", function() {
         wsbus.send("change-task-wiki", {
            "taskId" : task.id,
            "wiki" : txt.property("value")
         });
      });

      updateWikiCallback(null, task);

      bus.send("ui-button:create", {
         "div" : "btnEdit",
         "parentDiv" : container.attr("id"),
         "text" : "Editar",
         "sendEventName" : "edit-wiki"
      });
      bus.listen("edit-wiki", function(e, message) {
         setEditMode(true);
         cancelText = txt.property("value");
      });

      bus.send("ui-button:create", {
         "div" : "btnOk",
         "parentDiv" : container.attr("id"),
         "text" : "Aceptar",
         "sendEventName" : "accept-wiki"
      });
      bus.listen("accept-wiki", function(e, message) {
         setEditMode(false);
      });

      bus.send("ui-button:create", {
         "div" : "btnCancel",
         "parentDiv" : container.attr("id"),
         "text" : "Cancelar",
         "sendEventName" : "cancel-wiki"
      });
      bus.listen("cancel-wiki", function(e, message) {
         wsbus.send("change-task-wiki", {
            "taskId" : task.id,
            "wiki" : cancelText
         });
         setEditMode(false);
      });

      bus.send("ui-button:create", {
         "div" : "btnClose",
         "parentDiv" : container.attr("id"),
         "text" : "Cerrar",
         "sendEventName" : "close-wiki"
      });
      bus.listen("close-wiki", function(e, message) {
         bus.stopListen("updated-task", updateWikiCallback);
         d3.select("#" + wikiOverlayId).remove();
      });

      setEditMode(false);

      bus.listen("updated-task", updateWikiCallback);
   });

});
