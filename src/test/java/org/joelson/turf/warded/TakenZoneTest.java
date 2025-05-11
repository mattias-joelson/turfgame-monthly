package org.joelson.turf.warded;

import org.joelson.turf.util.URLReaderTest;

import java.io.IOException;
import java.util.Map;

public class TakenZoneTest {

    public static Map<String, Integer> readTakenZones() throws IOException {
        return URLReaderTest.readProperties("warded.unique.php.html", TakenZones::fromHTML);
    }
}
