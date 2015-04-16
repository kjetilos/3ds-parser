::
:: Script for rendering a 3d model using the java test class
::
:: Usage: 
::   > set JOGL_DIR=C:\jogl
::   > render.cmd src\test\resources\fighter
::

@echo off
set model=%1

if not defined JOGL_DIR (
  echo You need to set the JOGL_DIR environment variable
  exit /B 1
)

if "%model%" == "" (
  set model=src\test\resources\fighter
)

java -cp target\classes;target\test-classes;%JOGL_DIR%\jar\glugen.jar;%JOGL_DIR%\jar\gluegen-rt.jar;%JOGL_DIR%\jar\jogl-all.jar no.myke.parser.RenderModel %model%.3ds %model%.png