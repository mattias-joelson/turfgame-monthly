package org.joelson.turf.warded;

import org.joelson.turf.turfgame.apiv5.Zone;
import org.joelson.turf.turfgame.apiv5.ZonesTest;
import org.joelson.turf.util.KMLWriter;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UntakenStockholmZoneTest {

    @Test
    public void generateStockholmTakeMap() throws Exception {
        List<Zone> zones = ZonesTest.getAllZones();
        List<Zone> stockholmZones = zones.stream()
                .filter(zone -> zone.getRegion() != null && zone.getRegion().getId() == 141).toList();
        Map<String, Integer> takenZones = TakenZoneTest.readTakenZones();
        List<Zone> untakenStockholmZones = stockholmZones.stream()
                .filter(zone -> !takenZones.containsKey(zone.getName())).toList();

        Set<String> yellowMunicipalities = Set.of("Danderyds kommun", "Huddinge kommun", "Järfälla kommun",
                "Lidingö kommun", "Nacka kommun", "Sollentuna kommun", "Solna kommun", "Stockholms kommun",
                "Sundbybergs kommun", "Täby kommun", "Upplands Väsby kommun");
        List<Zone> untakenYellowZones = untakenStockholmZones.stream()
                .filter(zone -> yellowMunicipalities.contains(zone.getRegion().getArea().getName()))
                .sorted(Comparator.comparing(Zone::getName)).toList();

        List<String> untakenMunicipalities = List.of("Ekerö kommun", "Tyresö kommun", "Vaxholms kommun");

        //List<String> sevenSummitsZones = List.of("Bruketbacken", "FlottsbroTop", "Hagatop", "HightopThree",
        // "HillZone", "OakSkiTop", "Väsjöbacken");
        List<Integer> sevenSummitsZones = List.of(214, 1014, 12075, 24040, 30842, 50285, 75039);

//        Set<String> municipalities = new HashSet<>();
//        stockholmZones.stream().map(Zone::getRegion).map(Region::getArea).map(Area::getName).forEach
//        (municipalities::add);
//        List<String> sortMun = municipalities.stream().sorted().collect(Collectors.toList());
        //"[Botkyrka kommun, Danderyds kommun, Ekerö kommun, Haninge kommun, Huddinge kommun, Järfälla kommun,
        // Lidingö kommun, Nacka kommun, Norrtälje kommun, Nykvarns kommun, Nynäshamns kommun, Salems kommun, Sigtuna
        // kommun, Sollentuna kommun, Solna kommun, Stockholms kommun, Sundbybergs kommun, Södertälje kommun, Tyresö
        // kommun, Täby kommun, Upplands Väsby kommun, Upplands-Bro kommun, Vallentuna kommun, Vaxholms kommun,
        // Värmdö kommun, Österåkers kommun]]"

        KMLWriter out = new KMLWriter("untaken_stockholm.kml");
        out.writeFolder("Untaken Orange Municipalities");
        untakenYellowZones.forEach(zone -> out.writePlacemark(
                String.format("%s - %s", zone.getName(), zone.getRegion().getArea().getName()), "", zone.getLongitude(),
                zone.getLatitude()));
        untakenYellowZones.forEach(
                zone -> System.out.printf("%s - %s%n", zone.getName(), zone.getRegion().getArea().getName()));
        for (String untakenMunicipality : untakenMunicipalities) {
            out.writeFolder("Untaken " + untakenMunicipality);
            List<Zone> untakenMunicipalityZones = untakenStockholmZones.stream()
                    .filter(zone -> zone.getRegion().getArea().getName().equals(untakenMunicipality))
                    .sorted(Comparator.comparing(Zone::getName)).toList();
            untakenMunicipalityZones.forEach(zone -> out.writePlacemark(
                    String.format("%s - %s", zone.getName(), zone.getRegion().getArea().getName()), "",
                    zone.getLongitude(), zone.getLatitude()));
            System.out.printf("%s: %d untaken zones%n", untakenMunicipality, untakenMunicipalityZones.size());
        }
        out.writeFolder("7 summits");
        stockholmZones.stream().filter(zone -> sevenSummitsZones.contains(zone.getId()))
                .forEach(zone -> out.writePlacemark(String.format("%s - %d", zone.getName(), zone.getId()), "",
                        zone.getLongitude(), zone.getLatitude()));
        out.close();

        countMunicipalityZones(stockholmZones, takenZones);
    }

    private void countMunicipalityZones(List<Zone> stockholmZones, Map<String, Integer> takenZones) {
        String[] stockholmMunicipalities =
                new String[]{ "Solna kommun", "Sollentuna kommun", "Danderyds kommun", "Stockholms kommun",
                        "Sundbybergs kommun", "Järfälla kommun", "Upplands Väsby kommun", "Täby kommun",
                        "Vaxholms kommun", "Lidingö kommun", "Nacka kommun", "Tyresö kommun", "Huddinge kommun",
                        "Ekerö kommun", "Sigtuna kommun", "Vallentuna kommun", "Österåkers kommun", "Värmdö kommun",
                        "Haninge kommun", "Botkyrka kommun", "Upplands-Bro kommun", "Norrtälje kommun",
                        "Södertälje kommun", "Salems kommun", "Nynäshamns kommun", "Nykvarns kommun", };
        int sumZones = 0;
        int sumVisited = 0;
        System.out.println();
        for (String municipality : stockholmMunicipalities) {
            List<Zone> municipalityZones = stockholmZones.stream()
                    .filter(zone -> zone.getRegion().getArea().getName().equals(municipality)).toList();
            int noZones = municipalityZones.size();
            int noVisited = (int) municipalityZones.stream().filter(zone -> takenZones.containsKey(zone.getName()))
                    .count();
            sumZones += noZones;
            sumVisited += noVisited;
            System.out.printf("%21s: %4d %4d (%4d %4d)%n", municipality, noZones, noVisited, sumZones, sumVisited);
        }
        System.out.println();
    }
}
