define([ "d3" ], function() {

   function buildAdd(container, newElement) {
      var txtNew = container.append("input").attr("type", "text");
      container//
      .append("span")//
      .attr("class", "span-button")//
      .html("añadir")//
      .on("click", function() {
         newElement(txtNew.property("value"));
         txtNew.property("value", "");
      });
   }

   function refresh(container, list, entryClassName, params) {
      params = params ? params : {};

      var selection = container.selectAll("." + entryClassName).data(list);
      selection.exit().remove();
      selection.enter().append("div");
      selection.attr("class", entryClassName);
      selection.html(function(d) {
         if (params.nameGetter) {
            return params.nameGetter(d);
         } else {
            return d;
         }
      });

      if (params.selectionPostprocess) {
         params.selectionPostprocess(selection);
      }
   }

   return {
      "buildAdd" : buildAdd,
      "refresh" : refresh
   }

});