package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;
/**
 * sample xml file
 * &lt;broadband/&gt;
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class OrientationCode implements ChannelIdSubsetter {

    /**
     * Creates a new <code>OrientationCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public OrientationCode(Element config) {
	this.config = config;
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param channelId a <code>ChannelId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(ChannelId channelId, CookieJar cookies) {
       	if(channelId.channel_code.charAt(2) == SodUtil.getNestedText(config).charAt(0)) {
	    return true;
	}
	else return false;
    }

    private Element config;

}//OrientationCode
