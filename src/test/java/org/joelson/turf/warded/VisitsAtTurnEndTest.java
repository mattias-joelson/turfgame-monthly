package org.joelson.turf.warded;

import org.joelson.turf.turfgame.apiv5.Zone;
import org.joelson.turf.zundin.MonthlyTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VisitsAtTurnEndTest {

    @Test
    public void createVisitsBaseFile() throws IOException {
        Set<Zone> circleZones = HeatmapTest.getKrausTorgCircleZones();
        Map<String, Integer> takenZones = HeatmapTest.readTakenZones();
        Map<String, Integer> takenCircleZones = new HashMap<>();
        for (Zone zone : circleZones) {
            int takes = takenZones.getOrDefault(zone.getName(), 0);
            takenCircleZones.put(zone.getName(), takes);
        }
        String filename = String.format("src/test/resources/visits_%s_round_%d.properties", MonthlyTest.NICK,
                MonthlyTest.ROUND);
        try (PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8)) {
            takenCircleZones.entrySet().stream()
                    .sorted(VisitsAtTurnEndTest::compare)
                    .forEach(entry -> writer.printf("%s=%d%n", entry.getKey(), entry.getValue()));
        }
    }

    private static int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        int valueCompare = o1.getValue().compareTo(o2.getValue());
        if (valueCompare == 0) {
            return o1.getKey().compareTo(o2.getKey());
        }
        return o1.getValue().compareTo(o2.getValue());
    }
}
