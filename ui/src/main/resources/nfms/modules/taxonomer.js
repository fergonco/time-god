define([ "message-bus", "websocket-bus", "d3" ], function(bus, wsbus, d3) {

   var root = null;
   var taxonomyType = null;
   var keywords = [];
   var choiceQueue = [];

   function initialize() {
      keywords = [];
      choiceQueue = [];
   }

   bus.listen("show-taxonomy", function(e, control, type) {
      initialize();
      taxonomyType = type;

      var taxonomerId = "taxonomer";

      var bounds = control.getBoundingClientRect();
      var container = d3.select("body").append("div")//
      .attr("id", "taxonomy-overlay")//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("id", taxonomerId)//
      .style("top", bounds.bottom + "px")//
      .style("left", bounds.right + "px");

      bus.send("ui-element:create", {
         "div" : "taxonomy-keywords",
         "parentDiv" : taxonomerId
      });
      bus.send("ui-element:create", {
         "div" : "taxonomy-title",
         "parentDiv" : taxonomerId,
         "type" : "b"
      })

      wsbus.send("get-taxonomy", type);

      bus.send("ui-key-listen", {
         "div" : null,
         "keydownEventName" : "escape-cancel"
      });

   });

   bus.listen("escape-cancel", function(e, event) {
      if (event.keyCode == 27) {
         close();
      }
   });

   bus.listen("updated-taxonomy", function(e, t) {
      root = t;
      refresh(t);
   });

   function close() {
      d3.select("#taxonomy-overlay").remove();
      bus.send("ui-key-unlisten", {
         "div" : null,
         "keydownEventName" : "escape-cancel"
      });
   }

   function next() {
      if (choiceQueue.length > 0) {
         refresh(choiceQueue.pop());
      } else {
         bus.send("taxonomy-processed", [ taxonomyType, keywords ])
         close();
      }
   }

   function refresh(taxonomy) {
      bus.send("ui-set-content", {
         "div" : "taxonomy-title",
         "html" : taxonomy.text
      })
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
            bus.send("ui-remove", "btnDone");
            bus.send("ui-button:create", {
               "div" : "btnDone",
               "parentDiv" : "taxonomer",
               "text" : "Hecho",
               "sendEventName" : "btnDone-click"
            });
            bus.listen("btnDone-click", function() {
               next();
            });
         }
         bus.send("ui-remove", "btnRestartTaxonomer");
         bus.send("ui-button:create", {
            "div" : "btnRestartTaxonomer",
            "parentDiv" : "taxonomer",
            "text" : "Reiniciar",
            "sendEventName" : "btnRestartTaxonomer-click"
         });
         bus.listen("btnRestartTaxonomer-click", function() {
            initialize();
            refresh(root);
         });
      } else {
         next();
      }
      updateKeywords();
   }

   function updateKeywords() {
      var keywordSelection = d3.select("#taxonomy-keywords").selectAll(".keyword").data(keywords);
      keywordSelection.exit().remove();
      keywordSelection.enter().append("li");
      keywordSelection.attr("class", "keyword")//
      .html(function(k) {
         return k;
      });
   }

});