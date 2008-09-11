@echo off
set cmd=%0
set fullpath=%~pd0

set basedir=%fullpath%..

set antdir=%basedir%\ant
set antclasspath=%antdir%\lib\ant-launcher.jar

rem echo fullpath = %fullpath%
rem echo basedir = %basedir%

java -cp "%antclasspath%" -Dant.home="%antdir%" -Dwl.root.dir="%basedir%" org.apache.tools.ant.launch.Launcher -f "%antdir%\run.xml" @TARGET@ %1
pause
