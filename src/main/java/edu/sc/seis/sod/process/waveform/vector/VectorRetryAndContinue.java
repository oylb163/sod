package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRetryAndContinue extends VectorResultWrapper implements Threadable {

    public VectorRetryAndContinue(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        MeasurementStorage cookieJar) throws Exception {
        if(sodDb == null) {
            sodDb = SodDB.getSingleton();
        }
        WaveformVectorResult result = MotionVectorArm.runProcessorThreadCheck(subProcess,
                                                                              event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        if(!result.isSuccess()) {
            cookieJar.getECP().update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                  Standing.RETRY));
            return wrap(result);
        }
        return result;
    }

    protected WaveformVectorResult wrap(WaveformVectorResult result) {
        return new WaveformVectorResult(result.getSeismograms(),
                                        new StringTreeBranch(this,
                                                             true,
                                                             result.getReason()));
    }

    private SodDB sodDb;

    public boolean isThreadSafe() {
        return true;
    }
}
