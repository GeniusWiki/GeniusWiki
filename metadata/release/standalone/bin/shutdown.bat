@echo off
set DATABASE_PORT=9001
set CATALINA_HOME=%~dp0\../tomcat/
set CATALINA_BASE=%~dp0\../geniuswiki/

set LOG_ROOT=-Dgeniuswiki.log.dir=%~dp0../geniuswiki/logs

setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH=.;../geniuswiki/webapps/ROOT/WEB-INF/classes
for %%G in (../geniuswiki/webapps/ROOT/WEB-INF/lib/*.jar) DO set CLASSPATH=!CLASSPATH!;../geniuswiki/webapps/ROOT/WEB-INF/lib/%%G
java -cp %CLASSPATH% %LOG_ROOT% com.edgenius.wiki.installation.HsqlDBHelper %DATABASE_PORT% 
ENDLOCAL

%~dp0\../tomcat/bin/shutdown.bat
