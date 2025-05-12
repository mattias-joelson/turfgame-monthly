package org.joelson.turf.zundin;

import org.joelson.turf.turfgame.apiv5.Zone;
import org.joelson.turf.turfgame.apiv5.ZonesTest;
import org.joelson.turf.util.KMLWriter;
import org.joelson.turf.warded.HeatmapTest;
import org.joelson.turf.warded.TakenZoneTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonthlyVisitTest {

    private static final String OBEROFF = "Oberoff";
    private static final int ROUND = 119;

    private static void visitMunicipalityTest(String municipality, String filename, Set<Zone> municipalityZones)
            throws IOException {
        visitMunicipalityTest(municipality, filename, municipalityZones, false);
    }

    private static void visitMunicipalityTest(
            String municipality, String filename, Set<Zone> municipalityZones, boolean partitionUnvisited)
            throws IOException {
        Monthly monthly = MonthlyTest.getMonthly();

        Set<String> monthlyVisits = monthly.getZones().stream().map(MonthlyZone::getName).collect(Collectors.toSet());

        Set<Zone> visitedZones = municipalityZones.stream().filter(z -> monthlyVisits.contains(z.getName()))
                .collect(Collectors.toSet());
        Set<Zone> unvisitedZones = municipalityZones.stream().filter(z -> !monthlyVisits.contains(z.getName()))
                .collect(Collectors.toSet());
        Set<Zone> unvisitedPurpleZones = new HashSet<>();
        if (partitionUnvisited) {
            Map<String, Integer> takenZones = TakenZoneTest.readTakenZones();
            unvisitedZones.stream()
                    .filter(z -> takenZones.getOrDefault(z.getName(), 0) > 50)
                    .forEach(unvisitedPurpleZones::add);
            unvisitedZones.removeAll(unvisitedPurpleZones);
        }

        try (KMLWriter out = new KMLWriter(filename)) {
            out.writeFolder(municipality + " unvisited");
            unvisitedZones.stream().sorted(Comparator.comparing(Zone::getName)).forEach(
                    zone -> out.writePlacemark(zone.getName(), "", zone.getLongitude(), zone.getLatitude()));
            if (!unvisitedPurpleZones.isEmpty()) {
                out.writeFolder(municipality + " unvisited purple");
                unvisitedPurpleZones.stream().sorted(Comparator.comparing(Zone::getName)).forEach(
                        zone -> out.writePlacemark(zone.getName(), "", zone.getLongitude(), zone.getLatitude()));
            }
            out.writeFolder(municipality + " visited");
            visitedZones.stream().sorted(Comparator.comparing(Zone::getName)).forEach(
                    zone -> out.writePlacemark(zone.getName(), "", zone.getLongitude(), zone.getLatitude()));
        }
        System.out.println(
                filename + ": " + (unvisitedZones.size() + visitedZones.size()) + " (" + municipalityZones.size()
                        + ')');
        assertEquals(municipalityZones.size(),
                unvisitedZones.size() + unvisitedPurpleZones.size() + visitedZones.size());

//        String filePrefix = filename.substring(0, filename.indexOf(".kml"));
//        try (CSVWriter out = new CSVWriter(filePrefix + "_unvisited.csv")) {
//            unvisitedZones.stream()
//                    .sorted(Comparator.comparing(Zone::getName))
//                    .forEach(zone -> out.writePlacemark(zone.getName(), zone.getLongitude(), zone.getLatitude()));
//        }
//        try (CSVWriter out = new CSVWriter(filePrefix + "_unvisited_purple.csv")) {
//            unvisitedPurpleZones.stream()
//                    .sorted(Comparator.comparing(Zone::getName))
//                    .forEach(zone -> out.writePlacemark(zone.getName(), zone.getLongitude(), zone.getLatitude()));
//        }
//        try (CSVWriter out = new CSVWriter(filePrefix + "_visited.csv")) {
//            visitedZones.stream()
//                    .sorted(Comparator.comparing(Zone::getName))
//                    .forEach(zone -> out.writePlacemark(zone.getName(), zone.getLongitude(), zone.getLatitude()));
//        }
    }

    @Test
    public void visitDanderydTest() throws IOException {
        visitMunicipalityTest("Danderyd", "danderyd_month.kml", ZonesTest.getDanderydAreaZones());
    }

    @Test
    public void visitSolnaTest() throws IOException {
        visitMunicipalityTest("Solna", "solna_month.kml", ZonesTest.getSolnaAreaZones());
    }

    @Test
    public void visitSollentunaTest() throws IOException {
        visitMunicipalityTest("Sollentuna", "sollentuna_month.kml", ZonesTest.getSollentunadAreaZones());
    }

    @Test
    public void visitStockholmTest() throws IOException {
        visitMunicipalityTest("Stockholm", "stockholm_month.kml", ZonesTest.getStockholmAreaZones());
    }

    @Test
    public void visitSundbybergTest() throws IOException {
        visitMunicipalityTest("Sundbyberg", "sundbyberg_month.kml", ZonesTest.getSundbybergAreaZones());
    }

    @Test
    public void visitTabyTest() throws IOException {
        visitMunicipalityTest("Täby", "taby_month.kml", ZonesTest.getTabyAreaZones());
    }

    @Test
    public void combinedDSSVisitTest() throws IOException {
        visitMunicipalityTest("DSS", "dss_month.kml", ZonesTest.getDSSAreaZones());
    }

    @Test
    public void combinedCircleVisitTest() throws IOException {
        visitMunicipalityTest("circle", "circle_month.kml", HeatmapTest.getKrausTorgCircleZones(), true);
    }

    @Test
    public void combinedTrueCircleVisitTest() throws IOException {
        visitMunicipalityTest("true_circle", "true_circle_month.kml", HeatmapTest.getKrausTorgTrueCircleZones(), true);
    }

    @Test
    public void combinedFlippVisitTest() throws IOException {
        visitMunicipalityTest("flipp", "flipp_month.kml", Flipp08MissionTest.getFlippZones(), true);
    }

    @Test
    public void combinedCircleVisitHeatmapTest() throws IOException {
        Set<Zone> circleZones = HeatmapTest.getKrausTorgCircleZones();
        Map<String, Integer> takesZones = HeatmapTest.readTakenZones();
        Map<String, Integer> monthlyVisits = MonthlyTest.getMonthly().getZones().stream().collect(
                Collectors.toMap(MonthlyZone::getName, MonthlyZone::getVisits));

        List<CombinedVisitZone> untakenZones = new ArrayList<>();
        List<CombinedVisitZone> yellowZones = new ArrayList<>();
        List<CombinedVisitZone> orangeZones = new ArrayList<>();
        List<CombinedVisitZone> redZones = new ArrayList<>();
        List<CombinedVisitZone> visitedOncePurpleZones = new ArrayList<>();
        List<CombinedVisitZone> visitedPurpleZones = new ArrayList<>();
        List<CombinedVisitZone> unvisitedPurpleZones = new ArrayList<>();
        int numberVisitedOnceZones = 0;
        int numberVisitedZones = 0;

        for (Zone zone : circleZones) {
            String zoneName = zone.getName();
            if (takesZones.containsKey(zoneName)) {
                int takes = takesZones.get(zoneName);
                int visits = (monthlyVisits.containsKey(zoneName)) ? monthlyVisits.get(zoneName) : 0;
                if (takes < 11) {
                    yellowZones.add(new CombinedVisitZone(zone, takes, visits));
                } else if (takes < 21) {
                    orangeZones.add(new CombinedVisitZone(zone, takes, visits));
                } else if (takes < 51) {
                    redZones.add(new CombinedVisitZone(zone, takes, visits));
                } else if (visits >= 2) {
                    visitedPurpleZones.add(new CombinedVisitZone(zone, takes, visits));
                } else if (visits == 1) {
                    visitedOncePurpleZones.add(new CombinedVisitZone(zone, takes, visits));
                } else {
                    unvisitedPurpleZones.add(new CombinedVisitZone(zone, takes, visits));
                }
                if (visits > 0) {
                    numberVisitedOnceZones += 1;
                    if (visits > 1) {
                        numberVisitedZones += 1;
                    }
                }
            } else {
                untakenZones.add(new CombinedVisitZone(zone, 0, 0));
            }
        }
        System.out.println("Zones visited:                " + numberVisitedOnceZones);
        System.out.println("Zones visited more than once: " + numberVisitedZones);

        KMLWriter out = new KMLWriter("circle_combined_month.kml");
        if (!untakenZones.isEmpty()) {
            out.writeFolder(String.format("Untaken Zones (%d)", untakenZones.size()));
            untakenZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        if (!yellowZones.isEmpty()) {
            out.writeFolder(
                    String.format("Yellow Zones (%d, %d unvisited)", yellowZones.size(), countUnvisited(yellowZones)));
            yellowZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        if (!orangeZones.isEmpty()) {
            out.writeFolder(
                    String.format("Orange Zones (%d, %d unvisited)", orangeZones.size(), countUnvisited(orangeZones)));
            orangeZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        if (!redZones.isEmpty()) {
            out.writeFolder(String.format("Red Zones (%d, %d unvisited)", redZones.size(), countUnvisited(redZones)));
            redZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        if (!unvisitedPurpleZones.isEmpty()) {
            out.writeFolder(String.format("Unvisited Purple Zones (%d)", unvisitedPurpleZones.size()));
            unvisitedPurpleZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        if (!visitedOncePurpleZones.isEmpty()) {
            out.writeFolder(String.format("Visited Once Purple Zones (%d)", visitedOncePurpleZones.size()));
            visitedOncePurpleZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        if (!visitedPurpleZones.isEmpty()) {
            out.writeFolder(String.format("Visited Purple Zones (%d)", visitedPurpleZones.size()));
            visitedPurpleZones.stream().sorted().forEach(zone -> zone.write(out));
        }
        out.close();
    }

    private int countUnvisited(List<CombinedVisitZone> combinedVisitZones) {
        return (int) combinedVisitZones.stream().filter(combinedVisitZone -> combinedVisitZone.visits == 0).count();
    }

    private static class CombinedVisitZone implements Comparable<CombinedVisitZone> {
        private final Zone zone;
        private final int takes;
        private final int visits;

        private CombinedVisitZone(Zone zone, int takes, int visits) {
            this.zone = zone;
            this.takes = takes;
            this.visits = visits;
        }

        @Override
        public int compareTo(CombinedVisitZone that) {
            return (this.takes == that.takes) ? this.zone.getName().compareTo(that.zone.getName()) :
                    this.takes - that.takes;
        }

        public void write(KMLWriter out) {
            out.writePlacemark(String.format("%d - %s%s", takes, zone.getName(),
                            (visits > 0) ? " (" + visits + " visits)" : " (unvisited)"), "", zone.getLongitude(),
                    zone.getLatitude());
        }
    }
}
