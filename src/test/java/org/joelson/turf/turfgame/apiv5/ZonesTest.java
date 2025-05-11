package org.joelson.turf.turfgame.apiv5;

import org.joelson.turf.util.URLReaderTest;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZonesTest {

    private static final int STOCKHOLM_REGION_ID = 141;

    private static final String DANDERYDS_KOMMUN_AREA_NAME = "Danderyds kommun";
    private static final String SOLNA_KOMMUN_AREA_NAME = "Solna kommun";
    private static final String SOLLENTUNA_KOMMUN_AREA_NAME = "Sollentuna kommun";
    private static final String STOCKHOLMS_KOMMUN_AREA_NAME = "Stockholms kommun";
    private static final String SUNDBYBERGS_KOMMUN_AREA_NAME = "Sundbybergs kommun";
    private static final String TABY_KOMMUN_AREA_NAME = "Täby kommun";

    public static List<Zone> getAllZones() throws IOException {
        return URLReaderTest.readProperties("zones-all.v5.json", Zones::fromJSON);
    }

    public static List<Zone> getStockholmRegionZones() throws IOException {
        return getAllZones().stream().filter(z -> z.getRegion().getId() == STOCKHOLM_REGION_ID).toList();
    }

    public static Set<Zone> getDSSAreaZones() throws IOException {
        return getStockholmRegionZones().stream().filter(ZonesTest::isDSSZone).collect(Collectors.toSet());
    }

    public static Set<Zone> getDanderydAreaZones() throws IOException {
        return getMunicipalityZones(DANDERYDS_KOMMUN_AREA_NAME);
    }

    public static Set<Zone> getSolnaAreaZones() throws IOException {
        return getMunicipalityZones(SOLNA_KOMMUN_AREA_NAME);
    }

    public static Set<Zone> getSollentunadAreaZones() throws IOException {
        return getMunicipalityZones(SOLLENTUNA_KOMMUN_AREA_NAME);
    }

    public static Set<Zone> getStockholmAreaZones() throws IOException {
        return getMunicipalityZones(STOCKHOLMS_KOMMUN_AREA_NAME);
    }

    public static Set<Zone> getSundbybergAreaZones() throws IOException {
        return getMunicipalityZones(SUNDBYBERGS_KOMMUN_AREA_NAME);
    }

    public static Set<Zone> getTabyAreaZones() throws IOException {
        return getMunicipalityZones(TABY_KOMMUN_AREA_NAME);
    }

    private static Set<Zone> getMunicipalityZones(String municipalityName) throws IOException {
        return getStockholmRegionZones().stream()
                .filter(z -> municipalityName.equals(z.getRegion().getArea().getName()))
                .collect(Collectors.toSet());
    }

    public static boolean isDSSZone(Zone zone) {
        Region region = zone.getRegion();
        if (region != null && region.getId() == STOCKHOLM_REGION_ID) {
            Area area = region.getArea();
            String name = area.getName();
            return DANDERYDS_KOMMUN_AREA_NAME.equals(name)
                    || SOLNA_KOMMUN_AREA_NAME.equals(name)
                    || SUNDBYBERGS_KOMMUN_AREA_NAME.equals(name);
        }
        return false;
    }

    public static boolean isSSTZone(Zone zone) {
        Region region = zone.getRegion();
        if (region != null && region.getId() == STOCKHOLM_REGION_ID) {
            Area area = region.getArea();
            String name = area.getName();
            return SOLLENTUNA_KOMMUN_AREA_NAME.equals(name)
                    || STOCKHOLMS_KOMMUN_AREA_NAME.equals(name)
                    || TABY_KOMMUN_AREA_NAME.equals(name);
        }
        return false;
    }

}
