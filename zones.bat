@echo off

call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv4.Zones"
call mvn exec:java -Dexec.mainClass="org.joelson.turf.turfgame.apiv5.Zones"
dir zones*json
copy zones-all.json src\test\resources
copy zones-all.v5.json src\test\resources
