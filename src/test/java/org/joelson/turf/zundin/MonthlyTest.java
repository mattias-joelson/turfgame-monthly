package org.joelson.turf.zundin;

import org.joelson.turf.util.URLReaderTest;

import java.io.IOException;

public class MonthlyTest {

    private static final String NICK = "0beroff";
    private static final int ROUND = 187;
    private static final String FILENAME = String.format("monthly_%s_round%d.html", NICK, ROUND);

    public static Monthly getMonthly() throws IOException {
        return URLReaderTest.readProperties(FILENAME, s -> Monthly.fromHTML(NICK, ROUND, s));
    }
}

