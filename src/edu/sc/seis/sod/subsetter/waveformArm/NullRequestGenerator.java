package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;


/**
 * Describe class <code>NullRequestGenerator</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullRequestGenerator implements RequestGenerator {
    public NullRequestGenerator() {}

    public NullRequestGenerator(Element config) {}

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel, CookieJar cookieJar){
        return new RequestFilter[0];
    }
}// NullRequestGenerator
