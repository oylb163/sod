/**
 * OriginPointDistance.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OriginPointDistance extends AbstractOriginPoint implements OriginSubsetter{


    /**
     * Creates a new <code>OriginPointDistance</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public OriginPointDistance (Element config) throws Exception{
        super(config);
    }

    /**
     * Accepts an origin only if it lies within the geven distance range of the
     * given lat and lon.
     *
     */
    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        double oLat = origin.my_location.latitude;
        double oLon = origin.my_location.longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        if (getMin().convertTo(UnitImpl.DEGREE).get_value() <= distaz.delta &&
            getMax().convertTo(UnitImpl.DEGREE).get_value() >= distaz.delta) {
            return true;
        } else {
            return false;
        }
    }

}

