@echo off

call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv4.Zones"
timeout /t 5 /nobreak
call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv5.Zones"
timeout /t 5 /nobreak
call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv6.Zones"
dir zones-all.v?.json
copy zones-all.v?.json src\test\resources
