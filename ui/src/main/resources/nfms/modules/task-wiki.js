define([ "message-bus", "d3", "markdown" ], function(bus, d3) {

   var wikiOverlayId = "wiki-overlay";

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

   bus.listen("show-task-wiki", function(e, task) {
      d3.select("#" + wikiOverlayId).remove();
      var container = d3.select("body").append("div")//
      .attr("id", wikiOverlayId)//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("class", "wiki-content")//
      .style("display", "inline-block");

      render = container.append("div")//
      .attr("class", "content")//
      .html(markdown.toHTML(task.wiki != null ? task.wiki : ""));

      txt = container.append("textarea")//
      .attr("visibility", "hidden")//
      .html(task.wiki);

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
         task.wiki = txt.property("value");
         setEditMode(false);
      });

      btnCancel = container//
      .append("span")//
      .attr("class", "span-button")//
      .html("Cancelar")//
      .on("click", function() {
         setEditMode(false);
      });

      btnClose = container//
      .append("span")//
      .attr("class", "span-button")//
      .html("Cerrar")//
      .on("click", function() {
         d3.select("#" + wikiOverlayId).remove();
      });

      setEditMode(false);
   });
});
