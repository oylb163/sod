/**
 * ChannelGroupOR.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform.vector;

import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class WaveformVectorOR extends WaveformVectorFork {

    public WaveformVectorOR(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     MeasurementStorage cookieJar) throws Exception {

        // pass originals to the contained processors
        WaveformVectorProcess processor;
        LinkedList<StringTree> reasons = new LinkedList<StringTree>();
        Iterator it = processes.iterator();
        WaveformVectorResult result = new WaveformVectorResult(seismograms, new StringTreeLeaf(this, false));
        boolean orResult = false;
        while (it.hasNext()  && ! orResult) {
            processor = (WaveformVectorProcess)it.next();
            result = MotionVectorArm.runProcessorThreadCheck(processor,
                                                             event,
                                                             channelGroup,
                                                             original,
                                                             available,
                                                             result.getSeismograms(),
                                                             cookieJar);
            orResult |= result.isSuccess();
            reasons.addLast(result.getReason());
        } // end of while (it.hasNext())
        return new WaveformVectorResult(result.getSeismograms(),
                                                     new StringTreeBranch(this,
                                                                          orResult,
                                                                              (StringTree[])reasons.toArray(new StringTree[0])));

    }

    public boolean isThreadSafe() {
        return true;
    }
}

