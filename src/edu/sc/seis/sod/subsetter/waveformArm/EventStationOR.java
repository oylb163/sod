package edu.sc.seis.sod.subsetter.waveformArm;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.WaveformLogicalSubsetter;

/**
 * This subsetter is used to specify a sequence of EventStationSubsetters. This subsetter is accepted when even one
 * of the subsetters forming the sequence is accepted. If all the subsetters in the sequence are not accepted then
 * the eventStationOR is not accepted.
 *<pre>
 *  &lt;eventStationOR&gt;
 *      &lt;phaseExists&gt;
 *          &lt;modelName&gt;prem&lt;/modelName&gt;
 *          &lt;phaseName&gt;ttp&lt;/phaseName&gt;
 *      &lt;/phaseExists&gt;
 *      &lt;phaseInteraction&gt;
 *          &lt;modelName&gt;prem&lt;/modelName&gt;
 *          &lt;phaseName&gt;PcP&lt;/phaseName&gt;
 *          &lt;interactionStyle&gt;PATH&lt;/interactionStyle&gt;
 *          &lt;interactionNumber&gt;1&lt;/interactionNumber&gt;
 *          &lt;relative&gt;
 *              &lt;reference&gt;EVENT&lt;/reference&gt;
 *              &lt;depthRange&gt;
 *                  &lt;unitRange&gt;
 *                      &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *                      &lt;min&gt;-1000&lt;/min&gt;
 *                      &lt;max&gt;1000&lt;/max&gt;
 *                  &lt;/unitRange&gt;
 *              &lt;/depthRange&gt;
 *              &lt;distanceRange&gt;
 *                  &lt;unit&gt;DEGREE&lt;/unit&gt;
 *                  &lt;min&gt;60&lt;/min&gt;
 *                  &lt;max&gt;70&lt;/max&gt;
 *              &lt;/distanceRange&gt;
 *          &lt;/relative&gt;
 *      &lt;/phaseInteraction&gt;
 *  &lt;/eventStationOR&gt;
 *</pre>
 */


public final class EventStationOR extends  WaveformLogicalSubsetter
    implements EventStationSubsetter {

    public EventStationOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o, Station station)
        throws Exception{
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            if (filter.accept(o,station)) { return true; }
        }
        return false;
    }
}// EventStationOR
