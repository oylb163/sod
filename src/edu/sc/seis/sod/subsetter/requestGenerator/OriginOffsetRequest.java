package edu.sc.seis.sod.subsetter.requestGenerator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Interval;

/**
 * OriginOffsetRequest.java Created: Wed Apr 2 16:06:39 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class OriginOffsetRequest implements RequestGenerator {

    public OriginOffsetRequest(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("beginOffset")) {
                    SodElement sodElement = (SodElement)SodUtil.load(element,
                                                                     "");
                    beginOffset = (Interval)sodElement;
                } else if(element.getTagName().equals("endOffset")) {
                    SodElement sodElement = (SodElement)SodUtil.load(element,
                                                                     "");
                    endOffset = (Interval)sodElement;
                }
            }
        }
    }

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel,
                                           CookieJar cookieJar)
            throws Exception {
        Origin origin = EventUtil.extractOrigin(event);
        MicroSecondDate originDate = new MicroSecondDate(origin.origin_time);
        TimeInterval bInterval = beginOffset.getTimeInterval();
        TimeInterval eInterval = endOffset.getTimeInterval();
        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        RequestFilter[] filters = {new RequestFilter(channel.get_id(),
                                                     bDate.getFissuresTime(),
                                                     eDate.getFissuresTime())};
        return filters;
    }

    private Interval beginOffset, endOffset;

    private String beginPhase;
} // OriginOffsetRequest
