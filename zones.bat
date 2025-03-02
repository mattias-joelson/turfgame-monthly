@echo off

call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv4.Zones"
call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv5.Zones"
call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv6.Zones"
dir zones-all.v?.json
copy zones-all.v?.json src\test\resources
