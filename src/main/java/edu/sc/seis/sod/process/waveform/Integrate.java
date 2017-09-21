package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.bag.Calculus;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

/**
 * @author crotwell Created on Mar 18, 2005
 */
public class Integrate implements WaveformProcess, Threadable {

    public Integrate(Element config) throws ConfigurationException {
        this.config = config;
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            seismograms[i] = Calculus.integrate(seismograms[i]);
        }
        return new WaveformResult(true, seismograms, this);
    }

    Element config;
}
