package org.joelson.turf.warded;

import org.joelson.turf.turfgame.apiv5.Zone;
import org.joelson.turf.turfgame.apiv5.ZonesTest;
import org.joelson.turf.turfgame.util.ZoneUtil;
import org.joelson.turf.util.URLReaderTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TakenZoneTest {

    private static final int PURPLE_TAKES = 51;
    private static final int THOUSAND = 1000;

    public static Map<String, Integer> readTakenZones() throws IOException {
        return URLReaderTest.readProperties("warded.unique.php.html", TakenZones::fromHTML);
    }

    @Test
    public void takenStockholmPurpleTest() throws IOException {
        List<Zone> stockholmAreaZones = ZonesTest.getStockholmRegionZones();
        Map<String, Integer> takenZones = readTakenZones();
        Zone origoZone = stockholmAreaZones.stream().filter(z -> z.getId() == HeatmapTest.KRAUSTORG_ZONE_ID).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown zone with zoneId " + HeatmapTest.KRAUSTORG_ZONE_ID));
        Set<Zone> purpleZones = stockholmAreaZones.stream()
                .filter(z -> takenZones.getOrDefault(z.getName(), 0) >= PURPLE_TAKES)
                .collect(Collectors.toSet());
        Map<Double, Zone> zoneDistances = purpleZones.stream()
                .collect(Collectors.toMap(z -> ZoneUtil.calcDistance(z, origoZone), Function.identity()));
        List<Double> orderedDistances = zoneDistances.keySet().stream().sorted().toList();
        System.out.printf("Purple zones:            %5d%n", purpleZones.size());
        System.out.printf("Max distance:            %5d m (%s)%n",
                Math.round(orderedDistances.getLast()), zoneDistances.get(orderedDistances.getLast()).getName());
        if (orderedDistances.size() >= THOUSAND) {
            Double thousandDistance = orderedDistances.get(THOUSAND - 1);
            System.out.printf("Distance to %dth zone: %5d m (%s)%n",
                    THOUSAND, Math.round(thousandDistance), zoneDistances.get(thousandDistance).getName());
        }
    }
}
