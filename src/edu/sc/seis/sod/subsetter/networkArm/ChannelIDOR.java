package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * channelIDOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelIDOR&gt;
 *   &lt;bandcode&gt;&lt;value&gt;B&lt;/value&gt;&lt;/bandCode&gt;
 *   &lt;bandcode&gt;&lt;value&gt;N&lt;/value&gt;&lt;/bandCode&gt;    
 * &lt;/channelIDOR&gt;
 * </bold></pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class ChannelIDOR 
    extends NetworkLogicalSubsetter 
    implements ChannelIdSubsetter {
    
    /**
     * Creates a new <code>ChannelIDOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public ChannelIDOR (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>ChannelId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(ChannelId e,  CookieJar cookies) throws Exception{
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelIdSubsetter filter = (ChannelIdSubsetter)it.next();
	    if (!filter.accept(e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// ChannelIDOR
