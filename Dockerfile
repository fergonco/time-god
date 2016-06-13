FROM tomcat:7.0
ADD ui/target/time-god-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/time-god.war
