package edu.sc.seis.sod.tools;

import java.util.Map;

import junit.framework.TestCase;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.ParseException;

import edu.sc.seis.seisFile.client.DonutParser;

public class DonutParserTest extends TestCase {

    public void testSuccessfulParse() throws ParseException {
        DonutParser dp = new DonutParser();
        Map results = (Map)dp.parse("23.3/-43/0/-45.21");
        assertEquals(results.get("lat"), "23.3");
        assertEquals(results.get("lon"), "-43");
        assertEquals(results.get("min"), "0");
        assertEquals(results.get("max"), "-45.21");
    }

    public void testBadBox() throws JSAPException {
        DonutParser sp = new DonutParser();
        try {
            sp.parse("24/43/2223");
            fail("A donut missing a slash should raise an exception!");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().indexOf("24/43/2223") != -1);
        }
    }
}
