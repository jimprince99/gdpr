@echo off
set arg1=%1
set arg2=%2
shift
shift
java -jar gdpr.jar %arg1% %arg2%