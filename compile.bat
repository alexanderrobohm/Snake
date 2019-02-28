@echo off

set projectname=snake
set jarname=snake.jar
set entrypoint=snake.Snake

cd src
javac %projectname%\*.java
cd ..
if not exist bin mkdir bin
xcopy /y /e src bin
cd bin
for /R %%x in (*.java) do del %%x
cd ..\src
for /R %%x in (*.class) do del %%x
cd ..\bin
jar -cvfe ..\%jarname% %entrypoint% %projectname%
cd ..

REM (ﾉಥ益ಥ）ﾉ﻿ ┻━┻