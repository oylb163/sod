/**
 * AlwaysSuccess.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeBranch;

public class WaveformVectorAlwaysSuccess extends VectorResultWrapper implements Threadable {

    public WaveformVectorAlwaysSuccess(Element config)
            throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channel,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
            WaveformVectorResult result = MotionVectorArm.runProcessorThreadCheck(subProcess,
                                                                                  event,
                                                                                  channel,
                                                                                  original,
                                                                                  available,
                                                                                  seismograms,
                                                                                  cookieJar);
            return new WaveformVectorResult(result.getSeismograms(),
                                            new StringTreeBranch(this,
                                                                 true,
                                                                 result.getReason()));
    }

    public String toString() {
        return "AlwaysSuccess(" + subProcess.toString() + ")";
    }

    public boolean isThreadSafe() {
        return true;
    }

}