define([ "message-bus", "d3" ], function(bus) {

   var pokerName = null;
   var txtCredits = null;

   bus.listen("updated-poker", function(e, poker) {
      pokerName = poker.name;
      assert(txtCredits != null, "ui not yet built");
      var credits = poker.totalCredits ? poker.totalCredits : "";
      if (credits != txtCredits.property("value")) {
         txtCredits.property("value", credits);
      }
   });

   return {
      "addButtons" : function(divContainer) {
         divContainer//
         .append("span")//
         .html("Total credits:")//
         .on("click", function() {
            bus.send("show-pokers");
         });
         txtCredits = divContainer.append("input").attr("type", "text");
         txtCredits.on("change", function() {
            assert(pokerName != null, "pokerName is null!");
            bus.send("change-poker-total-credits", [ pokerName, txtCredits.property("value") ]);
         });
      }
   };
});