/**
 * PhaseSignalToNoise.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.LongShortTrigger;
import edu.sc.seis.fissuresUtil.bag.PhaseNonExistent;
import edu.sc.seis.fissuresUtil.bag.SimplePhaseStoN;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeLeaf;

/** Calculates triggers, via LongShortSignalToNoise, and checks to see if a
 * trigger exists within +- the time interval for the given phase name. Uses the
 * first phase returned, ignoring later phases, such as triplications.
 *
 * The first trigger within the time window of the phase, if there is one, is
 * added to the cookieJar with key "sod_phaseStoN_"+phaseName for use by later
 * subsetters or later velocity output. */
public class PhaseSignalToNoise  implements WaveformProcess, Threadable {

    
    public PhaseSignalToNoise(Element config) throws ConfigurationException, TauModelException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("phaseName")) {
                    phaseName = SodUtil.getNestedText(element);
                } else if(element.getTagName().equals("modelName")) {
                    modelName = SodUtil.getNestedText(element);
                }else if(element.getTagName().equals("ratio")) {
                    ratio = Float.parseFloat(SodUtil.getNestedText(element));
                } else if(element.getTagName().equals("shortOffsetBegin")) {
                    shortOffsetBegin = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("shortOffsetEnd")) {
                    shortOffsetEnd = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("longOffsetBegin")) {
                    longOffsetBegin = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("longOffsetEnd")) {
                    longOffsetEnd = SodUtil.loadTimeInterval(element);
                }
            }
        }
        taupUtil = TauPUtil.getTauPUtil(modelName);

        phaseStoN = new SimplePhaseStoN(phaseName,
                                        shortOffsetBegin,
                                        shortOffsetEnd,
                                        longOffsetBegin,
                                        longOffsetEnd,
                                        taupUtil);
    }
    
    public boolean isThreadSafe(){
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                         ChannelImpl channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar) throws Exception {
        if (seismograms.length == 0 ) {
            return new WaveformResult(seismograms, new StringTreeLeaf(this, false, "no seismograms"));
        }
        try {
        LongShortTrigger trigger = calcTrigger(event, channel, seismograms);
        if (trigger != null) {
            if (trigger.getValue() > ratio) {
                cookieJar.put(getCookieName(), trigger);
                return new WaveformResult(seismograms,
                                                 new StringTreeLeaf(this, true));
            }
            return new WaveformResult(seismograms,
                                             new StringTreeLeaf(this, false, "trigger="+trigger.getValue()+" < "+ratio));
        } else {
            return new WaveformResult(seismograms,
                                             new StringTreeLeaf(this, false, "trigger is null"));
        }

        } catch (PhaseNonExistent e) {
            // no phase at this distance, just fail
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf(this, false, "Phase does not exist"));
        }
    }

    /** This method exists to make the trigger available to other subsetters
     * or processors so they don't have to call accept, which adds it to the
     * cookieJar. */
    public LongShortTrigger calcTrigger(EventAccessOperations event,
                                        Channel channel,
                                        LocalSeismogramImpl[] seismograms) throws NoPreferredOrigin, FissuresException, PhaseNonExistent, TauModelException {
        // find the first seismogram with a non-null trigger, probably the first
        // that overlaps the timewindow, and return it.
        for (int i = 0; i < seismograms.length; i++) {
           LongShortTrigger trigger =
                phaseStoN.process(channel.getSite().getLocation(), event.get_preferred_origin(), seismograms[i]);
            if (trigger != null) { return trigger; }
        }
        return null;
    }
    
    public String getCookieName() {
        return PHASE_STON_PREFIX+getPhaseName();
    }

    public String getPhaseName() {
        return phaseName;
    }


    public String toString() {
        return "PhaseSignalToNoise("+getPhaseName()+")";
    }

    public static final String PHASE_STON_PREFIX = "sod_phaseStoN_";

    protected SimplePhaseStoN phaseStoN;

    protected float ratio = 1.0f;

    protected String phaseName;

    protected TimeInterval shortOffsetBegin, shortOffsetEnd;
    
    protected TimeInterval longOffsetBegin, longOffsetEnd;
    
    protected String modelName = "prem";

    protected TimeInterval triggerWindow;

    protected TauPUtil taupUtil;

}

