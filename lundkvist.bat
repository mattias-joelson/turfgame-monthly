@echo off

call mvn exec:java -Dexec.mainClass="org.joelson.turf.lundkvist.Municipality" -Dexec.args="user=0beroff country=se region=141 %*"
move /Y lundkvist_141* src\test\resources
dir /od src\test\resources\lundkvist_141*
