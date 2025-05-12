package org.joelson.turf;

import org.joelson.turf.warded.HeatmapTest;
import org.joelson.turf.warded.TakeDistributionTest;
import org.joelson.turf.warded.UntakenStockholmZoneTest;
import org.joelson.turf.zundin.MonthlyVisitTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UpdateMonthlyMapsTest {

    @Test
    public void updateMonthlyMapsTest() throws IOException {
        new UntakenStockholmZoneTest().generateStockholmTakeMap();
        new MonthlyVisitTest().combinedCircleVisitHeatmapTest();
        new TakeDistributionTest().circleTakeDistributionTest();
        new HeatmapTest().circleHeatmap();
    }
}
