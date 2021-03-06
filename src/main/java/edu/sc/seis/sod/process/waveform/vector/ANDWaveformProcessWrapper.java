/**
 * ANDLocalSeismogramSubsetterWrapper.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveform.ForkProcess;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class ANDWaveformProcessWrapper implements WaveformProcessWrapper {

    public ANDWaveformProcessWrapper(WaveformProcess subsetter) {
        this.process = subsetter;
    }

    public ANDWaveformProcessWrapper(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                process = (WaveformProcess)SodUtil.load((Element)node,
                                                        "waveform");
                break;
            }
        }
    }

    public WaveformProcess getProcess() {
        return process;
    }

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        boolean b = true;
        StringTree[] reason = new StringTree[channelGroup.getChannels().length];
        int i;
        for(i = 0; b && i < channelGroup.getChannels().length; i++) {
            LocalSeismogramImpl[] copies = ForkProcess.copySeismograms(seismograms[i]);
            ChannelImpl chan = channelGroup.getChannels()[i];
            WaveformResult result = LocalSeismogramArm.runProcessorThreadCheck(process,event,
                                                    chan,
                                                    original[i],
                                                    available[i],
                                                    copies,
                                                    cookieJar);
            out[i] = result.getSeismograms();
            reason[i] = result.getReason();
            b &= result.isSuccess();
        }
        for(int j=i+1; j<channelGroup.getChannels().length; j++) {
            reason[j] = new ShortCircuit(this);
        }
        if(!b) { return new WaveformVectorResult(seismograms,
                                                 new StringTreeBranch(this,
                                                                      false,
                                                                      reason)); }
        return new WaveformVectorResult(out, new StringTreeBranch(this,
                                                                        true,
                                                                        reason));
    }

    public String toString() {
        return "ANDWaveformProcessWrapper(" + process.toString() + ")";
    }

    WaveformProcess process;

    public WaveformProcess getWrappedProcess() {
        return process;
    }

    public boolean isThreadSafe() {
        return true;
    }
}