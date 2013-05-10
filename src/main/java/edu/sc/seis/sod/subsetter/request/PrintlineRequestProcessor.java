package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;

public class PrintlineRequestProcessor extends AbstractPrintlineProcess implements RequestSubsetter {

    public PrintlineRequestProcessor(Element config)
            throws ConfigurationException {
        super(config);
    }
    
    public static final String DEFAULT_TEMPLATE = "Got $originalRequests.size() from $channel for $event";

    public StringTree accept(CacheEvent event,
                          ChannelImpl channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             channel,
                             request,
                             cookieJar);
        return new Pass(this);
    }

    @Override
    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }
}
