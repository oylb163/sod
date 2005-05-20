/**
 * ChannelGroup.java
 * 
 * @author Jagadeesh Danala
 * @version
 */
package edu.sc.seis.sod;

import org.apache.log4j.Logger;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.sc.seis.fissuresUtil.bag.Rotate;
import edu.sc.seis.fissuresUtil.cache.EventUtil;

public class ChannelGroup {

    public ChannelGroup(Channel[] channels) {
        this.channels = channels;
        ecps = new EventChannelPair[channels.length];
    }

    public void addEventChannelPair(EventChannelPair ecp) {
        int index = getIndex(ecp.getChannel());
        if(index != -1) {
            ecps[index] = ecp;
        } else {
            throw new IllegalArgumentException("Tried to add event channel pair to channel group with channel not in channel group");
        }
    }

    public EventChannelPair getEventChannelPair(Channel chan) {
        int index = getIndex(chan);
        if(index != -1) {
            return ecps[index];
        } else {
            throw new IllegalArgumentException("Tried to get event channel pair for channel not in channel group");
        }
    }

    public Channel[] getChannels() {
        return channels;
    }

    public boolean contains(Channel c) {
        return getIndex(c) != -1;
    }

    /**
     * Finds the vertical channel. If no channel has a dip of -90 then null is
     * returned.
     */
    public Channel getVertical() {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].an_orientation.dip == -90) {
                return channels[i];
            }
        }
        return null;
    }

    /**
     * Finds the 2 horizontal channels.
     */
    public Channel[] getHorizontal() {
        Channel[] out = new Channel[2];
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].an_orientation.dip == 0) {
                if(out[0] == null) {
                    out[0] = channels[i];
                } else {
                    out[1] = channels[i];
                }
            }
        }
        if(out[1] == null) {
            if(out[0] == null) {
                return new Channel[0];
            } else {
                return new Channel[] {out[0]};
            }
        }
        return out;
    }

    /**
     * Gets the horizontals as X and Y, so that the second channel's azimmuth is
     * the first's + 90 degrees. If this is not possible, then a zero length
     * array is returned.
     */
    public Channel[] getHorizontalXY() {
        Channel[] out = getHorizontal();
        if(out.length != 2) {
            out = new Channel[0];
        } else if((out[0].an_orientation.azimuth + 90) % 360 == out[1].an_orientation.azimuth % 360) {
            // in right order
        } else if((out[1].an_orientation.azimuth + 90) % 360 == out[0].an_orientation.azimuth % 360) {
            Channel tmp = out[0];
            out[0] = out[1];
            out[1] = tmp;
        } else {
            out = new Channel[0];
        }
        return out;
    }

    /**
     * Gets the channel that corresponds to this channelId from the
     * ChannelGroup. The Event is needed in case this channel id comes from a
     * seismogram that has been rotated to GCP, ie it has R or T as its
     * orientation code.
     */
    public Channel getChannel(ChannelId chanId, EventAccessOperations event) {
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(chanId, channels[i].get_id())) {
                return channels[i];
            }
        }
        if(SiteIdUtil.areSameSite(chanId, channels[0].get_id())
                && chanId.channel_code.substring(0, 2)
                        .equals(channels[0].get_code().substring(0, 2))) {
            if(chanId.channel_code.endsWith("R")) {
                return getRadial(event);
            } else if(chanId.channel_code.endsWith("T")) {
                return getTransverse(event);
            }
        }
        return null;
    }

    public Channel getRadial(EventAccessOperations event) {
        return getRadial(EventUtil.extractOrigin(event).my_location);
    }

    public Channel getRadial(Location eventLoc) {
        Channel[] horizontal = getHorizontalXY();
        if(horizontal.length == 0) {
            return null;
        }
        Channel radial = new ChannelImpl(Rotate.replaceChannelOrientation(horizontal[0].get_id(),
                                                                          "R"),
                                         horizontal[0].name + "Radial",
                                         new Orientation((float)Rotate.getRadialAzimuth(horizontal[0].my_site.my_location,
                                                                                        eventLoc),
                                                         horizontal[0].an_orientation.dip),
                                         horizontal[0].sampling_info,
                                         horizontal[0].effective_time,
                                         horizontal[0].my_site);
        return radial;
    }

    public Channel getTransverse(EventAccessOperations event) {
        return getTransverse(EventUtil.extractOrigin(event).my_location);
    }

    public Channel getTransverse(Location eventLoc) {
        Channel[] horizontal = getHorizontalXY();
        if(horizontal.length == 0) {
            return null;
        }
        Channel transverse = new ChannelImpl(Rotate.replaceChannelOrientation(horizontal[0].get_id(),
                                                                              "T"),
                                             horizontal[0].name + "Transverse",
                                             new Orientation((float)Rotate.getTransverseAzimuth(horizontal[0].my_site.my_location,
                                                                                                eventLoc),
                                                             horizontal[0].an_orientation.dip),
                                             horizontal[0].sampling_info,
                                             horizontal[0].effective_time,
                                             horizontal[0].my_site);
        return transverse;
    }

    private int getIndex(Channel chan) {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].equals(chan))
                return i;
        }
        // didn't find by object equals, check for ids
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(channels[i].get_id(), chan.get_id())) {
                logger.warn("Found two channels with same id that are not equals()"
                        + ChannelIdUtil.toString(chan.get_id()));
                return i;
            }
        }
        return -1;
    }

    private Channel[] channels;

    private EventChannelPair[] ecps;

    private static final Logger logger = Logger.getLogger(ChannelGroup.class);
}