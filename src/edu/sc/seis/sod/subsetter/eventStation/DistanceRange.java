package edu.sc.seis.sod.subsetter.eventStation;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;

public class DistanceRange extends DistanceRangeSubsetter implements
        EventStationSubsetter {

    public DistanceRange(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations eventAccess,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Origin origin = null;
        origin = eventAccess.get_preferred_origin();
        double actualDistance = SphericalCoords.distance(origin.my_location.latitude,
                                                         origin.my_location.longitude,
                                                         station.my_location.latitude,
                                                         station.my_location.longitude);
        QuantityImpl dist = new QuantityImpl(actualDistance, UnitImpl.DEGREE);
        if(dist.greaterThanEqual(getMin()) && dist.lessThanEqual(getMax())) {
            logger.debug("Distance ok " + dist + " from " + getMin() + " "
                    + getMax());
            return true;
        } else {
            return false;
        }
    }

    private static Logger logger = Logger.getLogger(DistanceRange.class);
}// EventStationDistance
