define([ "message-bus", "d3" ], function(bus, d3) {

   bus.listen("websocket-connected", function() {
      d3.select("#wait-overlay").remove();
   });

   bus.listen("websocket-reconnect", function() {
      d3.select("#wait-overlay").remove();
      var container = d3.select("body").append("div")//
      .attr("id", "wait-overlay")//
      .attr("class", "modal-overlay")//
      .style("text-align", "center")//
      .append("div")//
      .attr("id", "message")//
      .style("display", "inline-block")//
      .html("Desconectado. Conectando en 5 segundos...");
   });
});
