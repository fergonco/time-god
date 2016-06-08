define([ "d3" ], function() {

   function create(container) {
      var add = null;
      var remove = null;
      var select = null;
      var renderer = null;
      var refresh = null;
      var entryClassName = null;
      var postProcess = null;

      var txtNew = container.append("input").attr("type", "text");
      container//
      .append("span")//
      .attr("class", "span-button")//
      .html("añadir")//
      .on("click", function() {
         add(txtNew.property("value"));
         txtNew.property("value", "");
      });

      var instance = {
         add : function(listener) {
            add = listener;
         },
         remove : function(listener) {
            remove = listener;
         },
         select : function(listener) {
            select = listener;
         },
         renderer : function(listener) {
            renderer = listener;
         },
         refresh : function(list) {
            var selection = container.selectAll("." + entryClassName).data(list);
            selection.exit().remove();
            selection.enter().append("div");
            selection.attr("class", entryClassName);
            selection.html(function(d) {
               return renderer(d);
            });
            selection.append("span")//
            .attr("class", "span-button")//
            .html("borrar")//
            .on("click", function(d) {
               remove(d);
            });
            selection.on("click", function(d) {
               select(d);
            });
            if (postProcess != null) {
               postProcess(selection);
            }
         },
         entryClassName : function(className) {
            entryClassName = className;
         },
         postProcess:function(postProcessFunction) {
            postProcess = postProcessFunction;
         }
      };

      return instance;
   }

   return {
      "create" : create,

   }

});