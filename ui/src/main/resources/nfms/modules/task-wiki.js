define([ "message-bus", "websocket-bus", "d3", "markdown" ], function(bus, wsbus, d3) {

   var wikiOverlayId = "wiki-overlay";

   var currentTaskId;
   var txt;
   var render;
   var btnOk;
   var btnCancel;
   var btnEdit;
   var btnClose;

   function setEditMode(flag) {

      txt.style("display", flag ? "block" : "none");
      render.style("display", flag ? "none" : "block");

      btnOk.style("display", flag ? "inline" : "none");
      btnCancel.style("display", flag ? "inline" : "none");
      btnEdit.style("display", flag ? "none" : "inline");
      btnClose.style("display", flag ? "none" : "inline");
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
      .attr("class", "wiki-content")//
      .style("display", "inline-block");

      render = container.append("div")//
      .attr("class", "content");

      txt = container.append("textarea")//
      .attr("visibility", "hidden")//
      .on("input", function() {
         wsbus.send("change-task-wiki", {
            "taskId" : task.id,
            "wiki" : txt.property("value")
         });
      });

      updateWikiCallback(null, task);

      btnEdit = container//
      .append("span")//
      .attr("class", "span-button")//
      .html("editar")//
      .on("click", function() {
         setEditMode(true);
      });

      btnOk = container//
      .append("span")//
      .attr("class", "span-button")//
      .html("Aceptar")//
      .on("click", function() {
         setEditMode(false);
      });

      btnCancel = container//
      .append("span")//
      .attr("class", "span-button")//
      .html("Cancelar")//
      .on("click", function() {
         wsbus.send("change-task-wiki", {
            "taskId" : task.id,
            "wiki" : task.wiki
         });
         setEditMode(false);
      });

      btnClose = container//
      .append("span")//
      .attr("class", "span-button")//
      .html("Cerrar")//
      .on("click", function() {
         bus.stopListen("updated-task", updateWikiCallback);
         d3.select("#" + wikiOverlayId).remove();
      });

      setEditMode(false);

      bus.listen("updated-task", updateWikiCallback);
   });

});
