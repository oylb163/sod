package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveformArm.AvailableDataSubsetter;
import org.w3c.dom.Element;

/**
 * Describe class <code>NullAvailableDataSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullAvailableDataSubsetter implements AvailableDataSubsetter {
    public NullAvailableDataSubsetter() {}

    public NullAvailableDataSubsetter(Element config) {}

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original,RequestFilter[] available, CookieJar cookieJar) {
        return true;
    }
}// NullAvailableDataSubsetter
