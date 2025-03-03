package org.joelson.turf.zundin;

import org.joelson.turf.util.URLReaderTest;

public class MonthlyTest {

    private static final String NICK = "0beroff";
    private static final int ROUND = 176;
    private static final String FILENAME = String.format("monthly_%s_round%d.html", NICK, ROUND);

    public static Monthly getMonthly() throws Exception {
        return URLReaderTest.readProperties(FILENAME, s -> Monthly.fromHTML(NICK, ROUND, s));
    }
}

