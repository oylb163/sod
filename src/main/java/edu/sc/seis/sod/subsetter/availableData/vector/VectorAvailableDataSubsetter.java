/**
 * ChannelGroupAvailableDataSubsetter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface VectorAvailableDataSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] request,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception;
}