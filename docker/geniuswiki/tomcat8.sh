#!/bin/bash
/usr/lib/jvm/java-8-oracle/bin/java \
-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager \
-Djava.endorsed.dirs=/usr/share/tomcat8/endorsed \
-classpath /usr/share/tomcat8/bin/bootstrap.jar:/usr/share/tomcat8/bin/tomcat-juli.jar \
-Xmx512m -XX:PermSize=128m -XX:+UseConcMarkSweepGC \
-Djava.awt.headless=true \
-Dgeniuswiki.data.root=/var/lib/geniuswiki/ \
-Dgeniuswiki.log.dir=/var/log/geniuswiki/ \
-Dcatalina.base=/var/lib/tomcat8 \
-Dcatalina.home=/usr/share/tomcat8 \
-Djava.util.logging.config.file=/var/lib/tomcat8/conf/logging.properties \
-Djava.io.tmpdir=/tmp/tomcat-tmp \
org.apache.catalina.startup.Bootstrap start