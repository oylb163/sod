package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SiteBoxArea implements ChannelSubsetter {

    public SiteBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(ChannelImpl chan, NetworkSource network) {
        return new StringTreeLeaf(this, AreaUtil.inArea(ba, chan.getSite().getLocation()));
    }

    private BoxArea ba;
}
