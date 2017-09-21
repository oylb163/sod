package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.ReduceTool;

public class Merge implements WaveformProcess, Threadable {

    
    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        return new WaveformResult(ReduceTool.merge(seismograms),
                                  new StringTreeLeaf(this,
                                                     true,
                                                     "Contiguous seismograms merged"));
    }

    public boolean isThreadSafe() {
        return true;
    }
}
