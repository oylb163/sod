/**
 * EventChannelGroupXOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class EventVectorXOR extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channelGroup,
                          MeasurementStorage cookieJar) throws Exception {
        EventVectorSubsetter filterA = (EventVectorSubsetter)filterList.get(0);
        StringTree resultA = filterA.accept(event, channelGroup, cookieJar);
        EventVectorSubsetter filterB = (EventVectorSubsetter)filterList.get(1);
        StringTree resultB = filterB.accept(event, channelGroup, cookieJar);
        return new StringTreeBranch(this, resultA.isSuccess() != resultB.isSuccess(), new StringTree[] {resultA, resultB});
    }
}