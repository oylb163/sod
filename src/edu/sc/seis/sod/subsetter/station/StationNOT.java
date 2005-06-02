package edu.sc.seis.sod.subsetter.station;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;

public final class StationNOT extends StationLogicalSubsetter implements
        StationSubsetter {

    public StationNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station e, NetworkAccess network) throws Exception {
        Iterator it = subsetters.iterator();
        if(it.hasNext()) {
            StationSubsetter filter = (StationSubsetter)it.next();
            if(filter.accept(e, null)) { return false; }
        }
        return true;
    }
}// StationNOT
