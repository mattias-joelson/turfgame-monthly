package org.joelson.turf.warded;

import org.joelson.turf.turfgame.apiv5.Zone;
import org.joelson.turf.turfgame.apiv5.ZonesTest;
import org.joelson.turf.turfgame.util.ZoneUtil;
import org.joelson.turf.util.KMLWriter;
import org.joelson.turf.util.URLReaderTest;
import org.joelson.turf.zundin.Monthly;
import org.joelson.turf.zundin.MonthlyTest;
import org.joelson.turf.zundin.MonthlyZone;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HeatmapTest {

    private static final int TAKES_ENTRIES = HeatmapCategories.PURPLE.getTakes() + 1;
    public static final double MIN_CIRCLE_RADIUS = 6600.0;
    public static final int KRAUSTORG_ZONE_ID = 123789;
    public static final int DESOMBLEVKVAR_ZONE_ID = 674048;

    public static Set<Zone> filterDSSZones(Collection<Zone> zones) {
        Set<Zone> dssZones = new HashSet<>();
        zones.stream().filter(ZonesTest::isDSSZone).forEach(dssZones::add);
        return dssZones;
    }

    public static Set<Zone> getKrausTorgCircleZones() throws IOException {
        return getCircleZones(KRAUSTORG_ZONE_ID);
    }

    public static Set<Zone> getDeSomBlevKvarCircleZones() throws IOException {
        return getCircleZones(DESOMBLEVKVAR_ZONE_ID);
    }

    private static Set<Zone> getCircleZones(int zoneId) throws IOException {
        List<Zone> stockholmRegionZones = ZonesTest.getStockholmRegionZones();
        Set<Zone> circleZones = filterDSSZones(stockholmRegionZones);

        Map<Integer, Zone> zoneIdMap = ZoneUtil.toIdMap(stockholmRegionZones);
        Zone origoZone = zoneIdMap.get(zoneId);
        if (origoZone == null) {
            throw new IllegalArgumentException("Unknown zone with zoneId " + zoneId);
        }
        double maxDistance = getMaxDistance(circleZones, origoZone, MIN_CIRCLE_RADIUS);
        System.out.println("Max distance: " + maxDistance);

        stockholmRegionZones.stream()
                .filter(ZonesTest::isSSTZone)
                .filter(z -> inDistance(z, origoZone, maxDistance))
                .forEach(circleZones::add);
        circleZones.add(zoneIdMap.get(120));
        circleZones.add(zoneIdMap.get(299));
        circleZones.add(zoneIdMap.get(64100));
        circleZones.add(zoneIdMap.get(94113));
        circleZones.add(zoneIdMap.get(131839));
        circleZones.add(zoneIdMap.get(223822));
        circleZones.add(zoneIdMap.get(680062));
        System.out.println("Zones: " + circleZones.size());
        return circleZones;
    }

    private static double getMaxDistance(Set<Zone> zones, Zone origoZone, double minimumRadius) {
        return Math.max(zones.stream().mapToDouble(z -> ZoneUtil.calcDistance(origoZone, z)).max().orElse(0),
                minimumRadius);
    }

    private static boolean inDistance(Zone zone, Zone origoZone, double maxDistance) {
        return ZoneUtil.calcDistance(zone, origoZone) <= maxDistance;
    }

    public static Set<Zone> getKrausTorgTrueCircleZones() throws IOException {
        return getTrueCircleZones(KRAUSTORG_ZONE_ID);
    }

    private static Set<Zone> getTrueCircleZones(int zoneId) throws IOException {
        List<Zone> stockholmRegionZones = ZonesTest.getStockholmRegionZones();
        Set<Zone> circleZones = filterDSSZones(stockholmRegionZones);

        Zone origoZone = circleZones.stream().filter(z -> z.getId() == zoneId).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown zone with zoneId " + zoneId));
        double maxDistance = getMaxDistance(circleZones, origoZone, MIN_CIRCLE_RADIUS);
        System.out.println("Max distance: " + maxDistance);

        stockholmRegionZones.stream().filter(z -> inDistance(z, origoZone, maxDistance)).forEach(circleZones::add);
        System.out.println("Zones: " + circleZones.size());
        return circleZones;
    }

    private static void initZoneMaps(List<Map<Zone, Integer>> zoneMaps, HeatmapCategories category) {
        initZoneMaps(zoneMaps, category.takes);
    }

    private static void initZoneMaps(
            List<Map<Zone, Integer>> zoneMaps, HeatmapCategories category, HeatmapCategories nextCategory) {
        initZoneMaps(zoneMaps, IntStream.range(category.getTakes(), nextCategory.getTakes()).toArray());
    }

    private static void initZoneMaps(List<Map<Zone, Integer>> zoneMaps, int... takes) {
        Map<Zone, Integer> map = new HashMap<>();
        for (int take : takes) {
            zoneMaps.add(map);
        }
    }

    private static int compareEntries(Entry<Zone, Integer> o1, Entry<Zone, Integer> o2) {
        int countDiff = o1.getValue() - o2.getValue();
        if (countDiff != 0) {
            return countDiff;
        }
        return o1.getKey().getName().compareTo(o2.getKey().getName());
    }

    public static Map<String, Integer> readTakenZones() throws IOException {
        return URLReaderTest.readProperties("warded.unique.php.html", TakenZones::fromHTML);
    }

    public static Map<String, Integer> calcRoundVisits(Set<Zone> zones, Map<String, Integer> takesZones) throws IOException {
        Map<String, Integer> previousVisits = getPreviousVisits();
        Map<String, Integer> monthlyVisits = new HashMap<>();
        for (Zone zone : zones) {
            String name = zone.getName();
            int takes = takesZones.getOrDefault(name, 0);
            int previous = previousVisits.getOrDefault(name, 0);
            if (takes > previous) {
                monthlyVisits.put(name, takes - previous);
            }
        }
        return monthlyVisits;
    }

    @Nonnull
    private static Map<String, Integer> getPreviousVisits() throws IOException {
        Map<String, Integer> previousVisits = new HashMap<>();
        String filename = String.format("src/test/resources/visits_%s_round_%d.properties", MonthlyTest.NICK, MonthlyTest.ROUND - 1);
        for (String line : Files.readAllLines(Path.of(filename))) {
            int index = line.indexOf('=');
            previousVisits.put(line.substring(0, index), Integer.parseInt(line.substring(index + 1)));
        }
        return previousVisits;
    }

    @Test
    public void calculateRoundVisits() throws IOException {
        Set<Zone> zones = getKrausTorgCircleZones();
        Map<String, Integer> previousVisits = getPreviousVisits();
        Map<String, Integer> currentVisits = readTakenZones();
        int previousYellowZones = 0, previousOrangeZones = 0, previousRedZones = 0, previousPurpleZones = 0;
        int yellowZones = 0, orangeZones = 0, redZones = 0, purpleZones = 0;
        System.out.println("previous");
        sumVisits(zones, previousVisits);
        System.out.println("current");
        sumVisits(zones, currentVisits);
    }

    private void sumVisits(Set<Zone> zones, Map<String, Integer> visits) {
        int yellowZones = 0, orangeZones = 0, redZones = 0, purpleZones = 0;
        int toOrange = 0, toRed = 0, toPurple = 0;
        for (Zone zone : zones) {
            String zoneName = zone.getName();
            int zoneVisits = visits.getOrDefault(zoneName, 0);
            if (zoneVisits < 11) {
                yellowZones += 1;
                toOrange += 11 - zoneVisits;
            }
            if (zoneVisits < 21) {
                orangeZones += 1;
                toRed += 21 - zoneVisits;
            }
            if (zoneVisits < 51) {
                redZones += 1;
                toPurple += 51 - zoneVisits;
            } else {
                purpleZones += 1;
            }
        }
        System.out.printf("%d yellow zones, %d visits to orange%n", yellowZones, toOrange);
        System.out.printf("%d orange zones, %d visits to red%n", orangeZones, toRed);
        System.out.printf("%d red zones, %d visits to purple%n", redZones, toPurple);
        System.out.printf("%d purple zones%n", purpleZones);
    }

    @Test
    public void danderydHeatmap() throws IOException {
        municipalityHeatmap("danderyd_heatmap.kml", readTakenZones(), ZonesTest.getDanderydAreaZones(),
                true);
    }

    @Test
    public void tabyHeatmap() throws IOException {
        municipalityHeatmap("taby_heatmap.kml", readTakenZones(), ZonesTest.getTabyAreaZones(), false);
    }

    @Test
    public void solnaHeatmap() throws IOException {
        municipalityHeatmap("solna_heatmap.kml", readTakenZones(), ZonesTest.getSolnaAreaZones(), true);
    }

    @Test
    public void sundbybergHeatmap() throws IOException {
        municipalityHeatmap("sundbyberg_heatmap.kml", readTakenZones(), ZonesTest.getSundbybergAreaZones(),
                true);
    }

    @Test
    public void dssHeatmap() throws IOException {
        municipalityHeatmap("dss_heatmap.kml", readTakenZones(), ZonesTest.getDSSAreaZones(), true);
    }

    @Test
    public void circleHeatmap() throws IOException {
        municipalityHeatmap("circle_heatmap.kml", readTakenZones(), getKrausTorgCircleZones(), true);
    }

    @Test
    public void circleVisitsHeatmap() throws IOException {
        Set<Zone> circleZones = getTorgCircleZones();
        Map<String, Integer> takenZones = readTakenZones();
        Map<String, Integer> roundVisits = calcRoundVisits(circleZones, takenZones);
        municipalityHeatmap("circle_visits_heatmap.kml", takenZones, circleZones, true, roundVisits);
    }

    @Nonnull
    private static Set<Zone> getTorgCircleZones() throws IOException {
        return getKrausTorgCircleZones();
    }

    @Test
    public void adjustedCircleHeatmap() throws IOException {
        municipalityHeatmap("adjusted_circle_heatmap.kml", readTakenZones(), getDeSomBlevKvarCircleZones(), true);
    }

    @Test
    public void trueCircleHeatmap() throws IOException {
        municipalityHeatmap("true_circle_heatmap.kml", readTakenZones(), getKrausTorgTrueCircleZones(), true);
    }

    @Test
    public void combinedMonthlyHeatmap() throws IOException {
        Set<Zone> combinedZones = getKrausTorgCircleZones();
        Set<String> combinedZoneNames = combinedZones.stream().map(Zone::getName).collect(Collectors.toSet());
        Monthly monthly = MonthlyTest.getMonthly();
        Map<String, Integer> monthlyTakenZones = monthly.getZones().stream().filter(
                monthlyZone -> combinedZoneNames.contains(monthlyZone.getName())).collect(
                Collectors.toMap(MonthlyZone::getName, MonthlyZone::getVisits));
        Map<String, Integer> notTakenZones = combinedZones.stream().filter(
                        z -> !monthlyTakenZones.containsKey(z.getName()))
                .collect(Collectors.toMap(Zone::getName, z -> 0));
        monthlyTakenZones.putAll(notTakenZones);
        Map<String, Integer> takenZones = readTakenZones().entrySet().stream().filter(
                entry -> combinedZoneNames.contains(entry.getKey())).collect(
                Collectors.toMap(Entry::getKey, Entry::getValue));
        Set<Zone> filteredZones = combinedZones.stream()
                .filter(z -> takenZones.get(z.getName()) == null
                        || takenZones.get(z.getName()) - monthlyTakenZones.get(z.getName()) <= 50)
                .collect(Collectors.toSet());
        municipalityHeatmap("monthlyCombinedHeatmap.kml", monthlyTakenZones, filteredZones, false);
    }

    @Test
    public void monthlyHeatmap() throws IOException {
        Monthly monthly = MonthlyTest.getMonthly();
        Map<String, Integer> takenZones = monthly.getZones().stream().collect(Collectors.toMap(MonthlyZone::getName,
                monthlyZone -> monthlyZone.getTakes() + monthlyZone.getAssists()));
        Set<String> takenZoneNames = takenZones.keySet();
        Map<String, Zone> zoneNameSet = ZoneUtil.toNameMap(ZonesTest.getAllZones());
        Set<Zone> zones = takenZoneNames.stream().map(zoneNameSet::get).collect(Collectors.toSet());
        municipalityHeatmap("monthlyHeatmap.kml", takenZones, zones, false);
    }

    @Test
    public void monthlySolnaHeatmap() throws IOException {
        Monthly monthly = MonthlyTest.getMonthly();
        Map<String, Integer> takenZones = monthly.getZones().stream().collect(Collectors.toMap(MonthlyZone::getName,
                monthlyZone -> monthlyZone.getTakes() + monthlyZone.getAssists()));
        municipalityHeatmap("monthlySolnaHeatmap.kml", takenZones, ZonesTest.getSolnaAreaZones(), false);
    }

    private void municipalityHeatmap(
            String filename, Map<String, Integer> takenZones, Set<Zone> zones, boolean printZones) throws IOException {
        Map<String, Integer> monthlyVisits = MonthlyTest.getMonthly().getZones().stream().collect(
                Collectors.toMap(MonthlyZone::getName, MonthlyZone::getVisits));

        municipalityHeatmap(filename, takenZones, zones, printZones, monthlyVisits);
    }

    private void municipalityHeatmap(
            String filename, Map<String, Integer> takenZones, Set<Zone> zones, boolean printZones,
            Map<String, Integer> monthlyVisits) throws IOException {
        List<Map<Zone, Integer>> zoneMaps = new ArrayList<>(TAKES_ENTRIES);
        Map<String, Integer> zoneMap = new HashMap<>();
        initZoneMaps(zoneMaps, HeatmapCategories.UNTAKEN);
        initZoneMaps(zoneMaps, HeatmapCategories.GREEN);
        initZoneMaps(zoneMaps, HeatmapCategories.YELLOW, HeatmapCategories.ORANGE);
        initZoneMaps(zoneMaps, HeatmapCategories.ORANGE, HeatmapCategories.RED_21);
        initZoneMaps(zoneMaps, HeatmapCategories.RED_21, HeatmapCategories.RED_27);
        initZoneMaps(zoneMaps, HeatmapCategories.RED_27, HeatmapCategories.RED_33);
        initZoneMaps(zoneMaps, HeatmapCategories.RED_33, HeatmapCategories.RED_39);
        initZoneMaps(zoneMaps, HeatmapCategories.RED_39, HeatmapCategories.RED_45);
        initZoneMaps(zoneMaps, HeatmapCategories.RED_45, HeatmapCategories.PURPLE);
        initZoneMaps(zoneMaps, HeatmapCategories.PURPLE);
        int[] zoneTakes = new int[TAKES_ENTRIES];
        int municipalityTakes = 0;

        int newZones = 0;
        int yellowVisits = 0;
        int orangeZones = 0;
        int orangeVisits = 0;
        int redZones = 0;
        int redVisits = 0;
        int purpleZones = 0;
        int purpleVisits = 0;
        int visitedZones = 0;

        for (Zone zone : zones) {
            String zoneName = zone.getName();
            int takes = 0;
            if (takenZones.containsKey(zoneName)) {
                takes = takenZones.get(zoneName);
                municipalityTakes += takes;
            }
            int cappedTakes = Math.min(takes, 51);
            zoneMaps.get(cappedTakes).put(zone, takes);
            zoneTakes[cappedTakes] += 1;
            zoneMap.put(zone.getName(), takes);
            if (monthlyVisits.containsKey(zoneName)) {
                visitedZones += 1;
                int visits = monthlyVisits.get(zoneName);
                int beginTakes = takes - visits;
                if (beginTakes == 0) {
                    newZones += 1;
                }
                if (beginTakes < 10) {
                    yellowVisits += Math.min(takes, 10) - beginTakes;
                }
                if (takes >= 11 && beginTakes < 21) {
                    if (beginTakes < 11) {
                        orangeZones += 1;
                    }
                    orangeVisits += Math.min(takes, 20) - Math.max(beginTakes, 10);
                }
                if (takes >= 21 && beginTakes < 51) {
                    if (beginTakes < 21) {
                        redZones += 1;
                    }
                    redVisits += Math.min(takes, 50) - Math.max(beginTakes, 20);
                }
                if (takes >= 51) {
                    if (beginTakes < 51) {
                        purpleZones +=1;
                    }
                    purpleVisits += takes - Math.max(beginTakes, 50);
                }
            }
        }

        int toOrange = countTakes(zoneTakes, WardedCategories.ORANGE);
        int toOrangeZones = countZones(zoneTakes, WardedCategories.ORANGE);
        int toRed = countTakes(zoneTakes, WardedCategories.RED);
        int toRedZones = countZones(zoneTakes, WardedCategories.RED);
        int toPurple = countTakes(zoneTakes, WardedCategories.PURPLE);
        int toPurpleZones = countZones(zoneTakes, WardedCategories.PURPLE);

        KMLWriter out = new KMLWriter(filename);
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.UNTAKEN.getTakes()), "untaken");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.GREEN.getTakes()), "green");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.YELLOW.getTakes()), "yellow");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.ORANGE.getTakes()), "orange");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.RED_21.getTakes()), "red 21-26");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.RED_27.getTakes()), "red 27-32");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.RED_33.getTakes()), "red 33-38");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.RED_39.getTakes()), "red 39-44");
        writeHeatmapFolder(out, zoneMaps.get(HeatmapCategories.RED_45.getTakes()), "red 45-50");
        writeHeatmapFolder(out, zoneMaps.get(WardedCategories.PURPLE.getTakes()), "purple");
        out.close();

//        String filenamePrefix = filename.substring(0, filename.indexOf(".kml"));
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.UNTAKEN.getTakes()), filenamePrefix + "_untaken");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.GREEN.getTakes()), filenamePrefix + "_green");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.YELLOW.getTakes()), filenamePrefix + "_yellow");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.ORANGE.getTakes()), filenamePrefix + "_orange");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.RED_21.getTakes()), filenamePrefix + "_red_21-26");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.RED_27.getTakes()), filenamePrefix + "_red_27-32");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.RED_33.getTakes()), filenamePrefix + "_red_33-38");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.RED_39.getTakes()), filenamePrefix + "_red_39-44");
//        writeHeatmapFolder(zoneMaps.get(HeatmapCategories.RED_45.getTakes()), filenamePrefix + "_red_45-50");
//        writeHeatmapFolder(zoneMaps.get(WardedCategories.PURPLE.getTakes()), filenamePrefix + "_purple");
//
        int[][] zoneCountArray = IntStream.range(0, zoneTakes.length).mapToObj(i -> new int[]{ i, zoneTakes[i] })
                .sorted(Comparator.comparingInt(a -> a[1])).toArray(int[][]::new);
        int max = zoneCountArray[51][1];
        if (max % 5 != 0) {
            max = ((max / 5) + 1) * 5;
        }
        int nextToMax = zoneCountArray[50][1];
        if (nextToMax % 5 != 0) {
            nextToMax = ((nextToMax / 5) + 1) * 5;
        }
        for (int i = max; i > 0; i -= 1) {
            if (i % 5 == 0) {
                if (i + 5 == max && i > nextToMax + 5) {
                    System.out.printf("     : %" + (zoneCountArray[51][0] + 1) + "s%n", ":");
                    i = nextToMax;
                    if (i == 0) {
                        continue;
                    }
                }
                System.out.printf("%4d + ", i);
            } else {
                System.out.print("     | ");
            }
            for (int zoneTake : zoneTakes) {
                System.out.print((zoneTake >= i) ? "*" : " ");
            }
            System.out.println();
        }
        System.out.println("     +-+----+----+----+----+----+----+----+----+----+----+-");
        System.out.println("       0    5   10   15   20   25   30   35   40   45   50");

        System.out.printf("File:            %s (%d / %d)%n", filename, visitedZones, zones.size());
        System.out.printf("Takes to orange: %d (%d zones, %d yellow visits, %d new zones, %.2f%%)%n",
                toOrange, toOrangeZones, yellowVisits, newZones,
                ((float) orangeZones + yellowVisits) * 100 / (toOrange + orangeZones + yellowVisits));
        System.out.printf("Takes to red:    %d (%d zones, %d orange visits, %d new orange zones, %.2f%%)%n",
                toRed, toRedZones, orangeVisits, orangeZones,
                ((float) redZones + orangeVisits + yellowVisits) * 100 / (toRed + redZones + orangeVisits + yellowVisits));
        System.out.printf("Takes to purple: %d (%d zones, %d red visits, %d new red zones. %.2f%%)%n",
                toPurple, toPurpleZones, redVisits, redZones,
                ((float) purpleZones + redVisits + orangeVisits + yellowVisits) * 100 / (toPurple + purpleZones + redVisits + orangeVisits + yellowVisits));
        System.out.printf("Total takes:     %d (%d new purple zones, %d purple visits)%n",
                municipalityTakes, purpleZones, purpleVisits);

        if (!printZones) {
            return;
        }
        List<Entry<String, Integer>> sortedZones = zoneMap.entrySet().stream().sorted(
                Entry.<String, Integer>comparingByValue().thenComparing(Entry.comparingByKey())).toList();
        int takes = 0;
        int printedZones = 0;
        for (Entry<String, Integer> entry : sortedZones) {
            if (entry.getValue() >= WardedCategories.PURPLE.getTakes()) {
                return;
            }
            if (entry.getValue() == takes) {
                System.out.println(entry.getValue() + " - " + entry.getKey());
                printedZones += 1;
            } else if (printedZones < 20 || entry.getValue() < WardedCategories.RED.getTakes()) {
                System.out.println(entry.getValue() + " - " + entry.getKey());
                takes = entry.getValue();
                printedZones += 1;
            } else {
                return;
            }
        }
    }

    private void writeHeatmapFolder(KMLWriter out, Map<Zone, Integer> zoneCounts, String folderName) {
        if (zoneCounts.isEmpty()) {
            return;
        }
        out.writeFolder(folderName);
        zoneCounts.entrySet().stream().sorted(HeatmapTest::compareEntries).forEach(zoneCountEntry -> out.writePlacemark(
                String.format("%d - %s", zoneCountEntry.getValue(), zoneCountEntry.getKey().getName()), "",
                zoneCountEntry.getKey().getLongitude(), zoneCountEntry.getKey().getLatitude()));
    }

    private int countZones(int[] zoneTakes, WardedCategories limit) {
        return IntStream.range(0, limit.getTakes()).map(i -> zoneTakes[i]).sum();
    }

    private int countTakes(int[] zoneTakes, WardedCategories limit) {
        int limitTakes = limit.getTakes();
        return IntStream.range(0, limitTakes).map(i -> (limitTakes - i) * zoneTakes[i]).sum();
    }

    private enum WardedCategories {
        UNTAKEN(0), GREEN(1), YELLOW(2), ORANGE(11), RED(21), PURPLE(51);

        private final int takes;

        WardedCategories(int takes) {
            this.takes = takes;
        }

        public int getTakes() {
            return takes;
        }
    }

    private enum HeatmapCategories {
        UNTAKEN(WardedCategories.UNTAKEN),
        GREEN(WardedCategories.GREEN),
        YELLOW(WardedCategories.YELLOW),
        ORANGE(WardedCategories.ORANGE),
        RED_21(WardedCategories.RED),
        RED_27(WardedCategories.RED, 27),
        RED_33(WardedCategories.RED, 33),
        RED_39(WardedCategories.RED, 39),
        RED_45(WardedCategories.RED, 45),
        PURPLE(WardedCategories.PURPLE);

        private final WardedCategories category;
        private final int takes;

        HeatmapCategories(WardedCategories category) {
            this(category, category.getTakes());
        }

        HeatmapCategories(WardedCategories category, int takes) {
            this.category = category;
            this.takes = takes;
        }

        public WardedCategories getCategory() {
            return category;
        }

        public int getTakes() {
            return takes;
        }
    }
}
