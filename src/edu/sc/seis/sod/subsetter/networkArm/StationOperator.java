package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * StationOperator.java
 * sample xml file
 *   &lt;stationOperator&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationOperator&gt;
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class StationOperator implements StationSubsetter {
    
    /**
     * Creates a new <code>StationOperator</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public StationOperator (Element config) throws ConfigurationException {
	this.config = config;
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e an <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Station e,  CookieJar cookies) {
	if(e.operator.equals(SodUtil.getNestedText(config))) return true;
	else return false;
    }

    Element config;

}// StationOperator

