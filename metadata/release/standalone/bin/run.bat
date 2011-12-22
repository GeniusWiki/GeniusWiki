@echo off
set DATABASE_PORT=9001
set CATALINA_HOME=%~dp0\../tomcat/
set CATALINA_BASE=%~dp0\../geniuswiki/
set CATALINA_OPTS=-Xms256M -Xmx512M -Dgeniuswiki.data.root=%~dp0\../data/ -Dgeniuswiki.log.dir=%~dp0../geniuswiki/logs

set LOG_ROOT=-Dgeniuswiki.log.dir=%~dp0../geniuswiki/logs
set DATA_ROOT=-Ddata.root=%~dp0../data/

cd %~dp0\../database
start java -cp %~dp0\../geniuswiki/webapps/ROOT/WEB-INF/lib/hsqldb-2.2.6.jar org.hsqldb.Server  -port %DATABASE_PORT% -database.0 database -dbname.0 geniuswiki
cd %~dp0\../bin

echo Initialise GeniusWiki...
setlocal ENABLEDELAYEDEXPANSION

REM logback-test.xml in bin directory for silence install log
set CLASSPATH=.;../geniuswiki/webapps/ROOT/WEB-INF/classes
set CLASSPATH=%CLASSPATH%;../geniuswiki/webapps/ROOT/WEB-INF/lib/*
REM for %%G in (../geniuswiki/webapps/ROOT/WEB-INF/lib/*.jar) DO set CLASSPATH=!CLASSPATH!;../geniuswiki/webapps/ROOT/WEB-INF/lib/%%G
java -cp %CLASSPATH%  %LOG_ROOT% %DATA_ROOT% com.edgenius.wiki.installation.SilenceInstall %~dp0\setup-variables.properties
ENDLOCAL

call %~dp0\../tomcat/bin/startup.bat