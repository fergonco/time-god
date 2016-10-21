define([ "message-bus" ], function(bus) {

   function removeGitHub(repo) {
      return repo.substring(7);
   }

   function newRepo(repoURL) {
      var repo = removeGitHub(repoURL);
      return {
         "getAPIIssuesLink" : function() {
            return "https://api.github.com/repos/" + repo + "issues";
         }
      }
   }

   function newIssue(issueURL) {
      issueURL = removeGitHub(issueURL);
      var repo = issueURL.substring(0, issueURL.lastIndexOf("/"));
      var issueNumber = issueURL.substring(issueURL.lastIndexOf("/") + 1);
      return {
         "getWebLink" : function() {
            return "https://github.com/" + repo + "/issues/" + issueNumber;
         },
         "getAPILink" : function() {
            return "https://api.github.com/repos/" + repo + "/issues/" + issueNumber;
         },
         "getNumber" : function() {
            return issueNumber;
         },
         "getRepo" : function() {
            return repo;
         }
      }
   }

   return {
      "repository" : newRepo,
      "issue" : newIssue
   }
});