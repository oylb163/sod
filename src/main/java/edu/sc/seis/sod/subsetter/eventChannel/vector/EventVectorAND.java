/**
 * EventChannelGroupAND.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class EventVectorAND extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             MeasurementStorage cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            EventVectorSubsetter f = (EventVectorSubsetter)filterList.get(i);
            result[i] = f.accept(event, channelGroup, cookieJar);
            if(!result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, false, result);
            }
        }
        return new StringTreeBranch(this, true, result);
    }
}