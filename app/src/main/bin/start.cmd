@echo off
setlocal ENABLEDELAYEDEXPANSION

set "MAIN_CLASS=net.kkolyan.jhole2.JHoleClientCP=.\conf"
for /r %%i in (.\lib\*.jar) do call set CP=!CP!;%%i

set "JVM_ARGS="
rem set "JVM_ARGS=%JVM_ARGS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
rem set "JVM_ARGS=%JVM_ARGS% -ea"
set "JVM_ARGS=%JVM_ARGS% -cp ^"%CP%^""

set "COMMAND=start javaw %JVM_ARGS% %MAIN_CLASS%"
rem echo %COMMAND%
%COMMAND%