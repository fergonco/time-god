define([ "message-bus", "websocket-bus", "d3" ], function(bus, wsbus, d3) {

   var task = null;
   var keywords = [];
   var choiceQueue = [];

   bus.listen("show-taxonomy", function(e, control, selectedTask, type) {
      task = selectedTask;
      keywords = [];

      // mostrar ventana
      var bounds = control.getBoundingClientRect();
      d3.select("body").append("div").attr("id", "modal-overlay")//
      .append("div")//
      .attr("class", "popup")//
      .attr("id", "taxonomer")//
      .style("top", bounds.bottom + "px")//
      .style("left", bounds.right + "px");

      // pedir taxonomia
      wsbus.send("get-taxonomy", type);
   });

   bus.listen("updated-taxonomy", function(e, taxonomy) {
      refresh(taxonomy);
   });

   function next() {
      if (choiceQueue.length > 0) {
         refresh(choiceQueue.pop());
      } else {
         wsbus.send("change-task-keywords", {
            "taskId" : task.id,
            "keywords" : keywords
         });
         d3.select("#modal-overlay").remove();
      }
   }

   function refresh(taxonomy) {
      if (taxonomy.type == "sequence") {
         if (taxonomy.children) {
            for (var i = 0; i < taxonomy.children.length; i++) {
               choiceQueue.push(taxonomy.children[i]);
            }
         }
         next();
      } else if (taxonomy.type == "choice" || taxonomy.type == "multiple-choice") {
         var className = "taxonomy-item";
         var selection = d3.select("#taxonomer").selectAll("." + className).data(taxonomy.children);
         selection.exit().remove();
         selection.enter().append("div");
         selection.attr("class", function(d) {
            var ret = className;
            var index = keywords.indexOf(d.name);
            ret += (index == -1) ? "" : " selected";
            return ret;
         });
         selection.html(function(d) {
            return d.text;
         })//
         .on("click", function(d) {
            if (taxonomy.type == "choice") {
               keywords.push(d.name);
               choiceQueue.push(d);
               next();
            } else if (taxonomy.type == "multiple-choice") {
               var index = keywords.indexOf(d.name);
               if (index == -1) {
                  keywords.push(d.name);
               } else {
                  keywords.splice(index, 1);
               }
               refresh(taxonomy);
            }
         });
         if (taxonomy.type == "multiple-choice") {
            d3.select("#btnDone").remove();
            d3.select("#taxonomer").append("span")//
            .attr("class", "span-button")//
            .attr("id", "btnDone")//
            .html("Hecho")//
            .on("click", function() {
               next();
            });
         }
      } else {
         next();
      }
   }

});