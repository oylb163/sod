package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
/**
 * ChannelFilter.java
 *
 *
 * Created: Thu Dec 13 17:15:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface ChannelFilter {

    public boolean accept(Channel channel, CookieJar cookies);
    
}// ChannelFilter
