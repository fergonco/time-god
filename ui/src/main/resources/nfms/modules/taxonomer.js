define([ "message-bus", "websocket-bus", "d3" ], function(bus, wsbus, d3) {

   var taxonomyType = null;
   var keywords = [];
   var choiceQueue = [];

   var title;

   bus.listen("show-taxonomy", function(e, control, type) {
      keywords = [];
      taxonomyType = type;

      var bounds = control.getBoundingClientRect();
      var container = d3.select("body").append("div")//
      .attr("id", "taxonomy-overlay")//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("id", "taxonomer")//
      .style("top", bounds.bottom + "px")//
      .style("left", bounds.right + "px");

      title = container.append("b");

      wsbus.send("get-taxonomy", type);
   });

   bus.listen("updated-taxonomy", function(e, taxonomy) {
      refresh(taxonomy);
   });

   function next() {
      if (choiceQueue.length > 0) {
         refresh(choiceQueue.pop());
      } else {
         bus.send("taxonomy-processed", [ taxonomyType, keywords ])
         d3.select("#taxonomy-overlay").remove();
      }
   }

   function refresh(taxonomy) {
      title.html(taxonomy.text);
      if (taxonomy.type == "sequence") {
         if (taxonomy.children) {
            for (var i = taxonomy.children.length - 1; i >= 0; i--) {
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