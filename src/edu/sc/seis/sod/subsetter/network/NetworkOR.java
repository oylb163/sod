package edu.sc.seis.sod.subsetter.network;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;

public final class NetworkOR extends  NetworkLogicalSubsetter
    implements NetworkSubsetter {

    public NetworkOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(NetworkAttr net) throws Exception{
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            NetworkSubsetter filter = (NetworkSubsetter)it.next();
            if ( filter.accept(net)) { return true; }
        }
        return false;
    }
}// NetworkAttrOR
