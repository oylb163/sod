package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class EventChannelLogicalSubsetter extends LogicalSubsetter {

    public EventChannelLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public String getArmName() {
        return "eventChannel";
    }
}