package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;
/**
 * sample xml file
 * <pre>
 * &lt;gainCode&gt;&lt;value&gt;Z &lt;/value&gt;&lt;/gainCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class ChannelCode implements ChannelSubsetter {

    /**
     * Creates a new <code>GainCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public ChannelCode(Element config) {
        this.config = config;
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAccess network, Channel channel, CookieJar cookies) throws Exception {
        if(channel.get_id().channel_code.equals(SodUtil.getNestedText(config))) {
            return true;
        }
        else return false;

    }

    private Element config;
}//GainCode
