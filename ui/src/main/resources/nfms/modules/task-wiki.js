define([ "message-bus", "websocket-bus", "d3", "ui-values", "markdown" ], function(bus, wsbus, d3, uiValues) {

   var wikiOverlayId = "wiki-overlay";

   var currentTaskId;
   var cancelText;

   var timerId = null;

   function setEditMode(flag) {
      bus.send(flag ? "ui-show" : "ui-hide", "txtWiki");
      bus.send(flag ? "ui-hide" : "ui-show", "wiki-render");

      bus.send(flag ? "ui-show" : "ui-hide", "btnOk");
      bus.send(flag ? "ui-show" : "ui-hide", "btnCancel");
      bus.send(flag ? "ui-hide" : "ui-show", "btnEdit");
      bus.send(flag ? "ui-hide" : "ui-show", "btnClose");
   }

   bus.listen("ui-update-task", function(e, task) {
      if (currentTaskId != null && currentTaskId == task.id) {
         bus.send("ui-set-content", {
            div : "wiki-render",
            html : markdown.toHTML(task.wiki != null ? task.wiki : "")
         });
         bus.send("ui-set-content", {
            div : "wiki-title",
            html : task.name
         });
         if (uiValues.get("txtWiki") != task.wiki) {
            uiValues.set("txtWiki", task.wiki);
            cancelText = uiValues.get("txtWiki");

            bus.send("ui-css", {
               "div" : "txtWiki",
               "property" : "background-color",
               "value" : "red"
            });
            setTimeout(function() {
               bus.send("ui-css", {
                  "div" : "txtWiki",
                  "property" : "background-color",
                  "value" : ""
               });
            }, 1000);
         }
      }
   });

   bus.listen("wikiChangeDone", function(e, message) {
      wsbus.send("change-task-wiki", {
         "taskId" : currentTaskId,
         "wiki" : message
      });
      bus.send("ui-css", {
         "div" : "wiki-title",
         "property" : "background-color",
         "value" : "blue"
      });
   });

   bus.listen("wikiChanged", function(e, message) {
      bus.send("ui-css", {
         "div" : "wiki-title",
         "property" : "background-color",
         "value" : "red"
      });
      if (timerId != null) {
         window.clearInterval(timerId);
      }
      timerId = window.setTimeout(function() {
         bus.send("wikiChangeDone", message);
      }, 2000);

   });

   bus.listen("edit-wiki", function(e, message) {
      setEditMode(true);
   });
   bus.listen("accept-wiki", function(e, message) {
      setEditMode(false);
   });
   bus.listen("cancel-wiki", function(e, message) {
      wsbus.send("change-task-wiki", {
         "taskId" : currentTaskId,
         "wiki" : cancelText
      });
      setEditMode(false);
   });
   bus.listen("close-wiki", function(e, message) {
      currentTaskId = null;
      d3.select("#" + wikiOverlayId).remove();
   });

   bus.listen("show-task-wiki", function(e, taskId) {
      currentTaskId = taskId;
      var container = d3.select("body").append("div")//
      .attr("id", wikiOverlayId)//
      .attr("class", "modal-overlay")//
      .append("div")//
      .attr("id", "wiki-content")//
      .attr("class", "wiki-content")//
      .style("display", "inline-block");

      bus.send("ui-element:create", {
         "div" : "wiki-title",
         "parentDiv" : "wiki-content",
         "type" : "h1"
      });
      
      bus.send("ui-element:create", {
         "div" : "wiki-render",
         "parentDiv" : "wiki-content",
         "type" : "div"
      });

      bus.send("ui-text-area-field:create", {
         "div" : "txtWiki",
         "parentDiv" : "wiki-content",
         "changeEventName" : "wikiChanged"
      });

      bus.send("ui-button:create", {
         "div" : "btnEdit",
         "parentDiv" : container.attr("id"),
         "text" : "Editar",
         "sendEventName" : "edit-wiki"
      });

      bus.send("ui-button:create", {
         "div" : "btnOk",
         "parentDiv" : container.attr("id"),
         "text" : "Aceptar",
         "sendEventName" : "accept-wiki"
      });

      bus.send("ui-button:create", {
         "div" : "btnCancel",
         "parentDiv" : container.attr("id"),
         "text" : "Cancelar",
         "sendEventName" : "cancel-wiki"
      });

      bus.send("ui-button:create", {
         "div" : "btnClose",
         "parentDiv" : container.attr("id"),
         "text" : "Cerrar",
         "sendEventName" : "close-wiki"
      });

      setEditMode(false);

      bus.send("get-task", taskId);
   });

});
