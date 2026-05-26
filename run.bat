@echo off
echo Starting Smart Assignment Checker...
echo This might take a few moments to compile and launch.
set JAVA_HOME=D:\Program Files\Java\jdk-23
.\mvnw.cmd clean compile javafx:run
pause
