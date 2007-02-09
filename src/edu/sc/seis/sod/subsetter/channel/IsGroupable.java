package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.LinkedList;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ChannelGrouper;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class IsGroupable implements ChannelSubsetter {

	public StringTree accept(Channel channel, ProxyNetworkAccess network)
			throws Exception {
        Channel[] allChans = network.retrieve_for_station(channel.my_site.my_station.get_id());
        ArrayList siteChans = new ArrayList();
        for (int i = 0; i < allChans.length; i++) {
			if (SiteIdUtil.areSameSite(allChans[i].get_id(), channel.get_id())) {
				siteChans.add(allChans[i]);
			}
		}

        LinkedList failures = new LinkedList();
        ChannelGroup[] chanGroups = channelGrouper.group(allChans, failures);
        for (int i = 0; i < chanGroups.length; i++) {
        	if (chanGroups[i].contains(channel)) {
        		return new Pass(this);
        	}
		}
        return new Fail(this);
	}


    private ChannelGrouper channelGrouper = new ChannelGrouper();
}