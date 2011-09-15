#!/bin/sh
DATABASE_PORT=9001

cd ..
ROOT=`pwd`
cd bin

export CATALINA_HOME="${ROOT}/tomcat"
export CATALINA_BASE="${ROOT}/geniuswiki"

LOG_ROOT=-Dgeniuswiki.log.dir="${ROOT}/geniuswiki/logs"

CLASSPATH=.:../geniuswiki/webapps/ROOT/WEB-INF/classes
CLASSPATH=${CLASSPATH}:$( echo ../geniuswiki/webapps/ROOT/WEB-INF/lib/*.jar . | sed 's/ /:/g')
java -cp ${CLASSPATH} ${LOG_ROOT} com.edgenius.wiki.installation.HsqlDBHelper ${DATABASE_PORT} 


../tomcat/bin/shutdown.sh
