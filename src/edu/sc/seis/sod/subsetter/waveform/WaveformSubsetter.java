package edu.sc.seis.sod.subsetter.waveform;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * LocalSeismogramSubsetter.java
 *
 *
 * Created: Thu Dec 13 18:01:05 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface WaveformSubsetter extends Subsetter{
    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms, CookieJar cookieJar) throws Exception;
}// LocalSeismogramSubsetter
