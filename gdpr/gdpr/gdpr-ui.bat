@echo off
set arg1=%1
set arg2=%2
shift
shift
java -jar gdpr-ui.jar %arg1% %arg2%