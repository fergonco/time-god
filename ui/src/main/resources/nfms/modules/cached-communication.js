define([ "message-bus" ], function(bus) {

   var urls = {}

   function getSeparator(url) {
      return url.indexOf("?") != -1 ? "&" : "?";
   }

   bus.listen("cached-ajax", function(e, message) {
      var url = message.url;
      var version = null;
      if (urls.hasOwnProperty(url)) {
         version = urls[url];
      } else {
         version = new Date().getTime();
         urls[url] = version;
      }
      message.url += getSeparator(message.url) + "_foo=" + version;
      bus.send("ajax", message);
   });

   bus.listen("clear-ajax-cache", function(e, message) {
      urls = {};
   });
});