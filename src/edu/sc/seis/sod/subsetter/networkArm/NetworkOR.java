package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * networkAttrOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *
 * sample xml file
 *<body><pre><bold>
 * &lt;networkAttrOR&gt;
 *  &lt;networkeffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *              &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkeffectiveTimeOverlap&gt;
 *  &lt;networkeffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *              &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkeffectiveTimeOverlap&gt;
 * &lt;/networkAttrOR&gt;
 * </bold></pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
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
