package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * StationName.java
 * sample xml file
 * <pre>
 *   &lt;stationName&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationName&gt;
 * </pre>
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class StationName implements StationSubsetter {
    public StationName (Element config) throws ConfigurationException {
        this.config = config;
    }

    public boolean accept(Station e) {
        if(e.name.equals(SodUtil.getNestedText(config))) return true;
        else return false;
    }

    Element config;
}// StationName
