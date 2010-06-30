package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class VectorAvailableDataScript extends AbstractScriptSubsetter implements VectorAvailableDataSubsetter {

    public VectorAvailableDataScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channelGroup", channelGroup);
        engine.put("original", original);
        engine.put("available", available);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}