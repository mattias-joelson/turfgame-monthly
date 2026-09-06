@echo off

set user=0beroff
set round=194
set filename=monthly_%user%_round%round%.html

call mvn exec:java -Dexec.mainClass="org.joelson.turf.zundin.Monthly" -Dexec.args="user=%user% round=%round%"
move src\test\resources\%filename% src\test\resources\%filename%.bak
move %filename% src\test\resources
dir /od src\test\resources\%filename%*
