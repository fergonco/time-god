define([], function() {

   /*
    * Technically a valid ID, points have to be escaped in order to be matched
    */
   function toIdNoPoints(input) {
      input = toId(input);
      input = input.replace(/\./g, "_");
      return input;
   }

   function toId(input) {
      input = latinize(input);
      input = input.replace(/\s/g, "_");
      return input;
   }

   function latinize(input) {
      return input.replace(/[^A-Za-z0-9\[\] ]/g, function(a) {
         return latin_map[a] || a
      });
   }
   // http://stackoverflow.com/questions/286921/efficiently-replace-all-accented-characters-in-a-string/9667817#9667817
   var latin_map = {
      "Á" : "A",
      "É" : "E",
      "Í" : "I",
      "Ó" : "O",
      "Ú" : "U",
      "Ň" : "N",
      "á" : "a",
      "é" : "e",
      "í" : "i",
      "ó" : "o",
      "ú" : "u",
      "ñ" : "n",
   };

   return {
      "latinize" : latinize,
      "toId" : toId,
      "toIdNoPoints" : toIdNoPoints
   }
});