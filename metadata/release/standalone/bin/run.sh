#!/bin/sh
DATABASE_PORT=9001

cd ..
ROOT=`pwd`
cd bin

export CATALINA_HOME="${ROOT}/tomcat"
export CATALINA_BASE="${ROOT}/geniuswiki"
export CATALINA_OPTS="-Xms256M -Xmx512M -Dgeniuswiki.data.root=${ROOT}/data/ -Dgeniuswiki.log.dir=${ROOT}/geniuswiki/logs"

LOG_ROOT=-Dgeniuswiki.log.dir="${ROOT}/geniuswiki/logs"
DATA_ROOT="-Ddata.root=${ROOT}/data/"

cd ../database
java -cp ../geniuswiki/webapps/ROOT/WEB-INF/lib/hsqldb-2.2.6.jar org.hsqldb.Server  -port ${DATABASE_PORT} -database.0 database -dbname.0 geniuswiki &
cd ../bin

echo Initialise GeniusWiki...
CLASSPATH=.:../geniuswiki/webapps/ROOT/WEB-INF/classes
CLASSPATH=${CLASSPATH}:$( echo ../geniuswiki/webapps/ROOT/WEB-INF/lib/*.jar . | sed 's/ /:/g')
java -cp ${CLASSPATH}  ${LOG_ROOT} ${DATA_ROOT} com.edgenius.wiki.installation.SilenceInstall ${ROOT}/bin/setup-variables.properties

../tomcat/bin/startup.sh